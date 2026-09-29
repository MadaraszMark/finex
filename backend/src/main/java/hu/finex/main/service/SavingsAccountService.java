package hu.finex.main.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hu.finex.main.config.FinexProperties;
import hu.finex.main.dto.CreateSavingsAccountRequest;
import hu.finex.main.dto.SavingsAccountResponse;
import hu.finex.main.dto.SavingsTransactionResponse;
import hu.finex.main.dto.SavingsTransferRequest;
import hu.finex.main.dto.SavingsTransferResponse;
import hu.finex.main.dto.UpdateSavingsAccountRequest;
import hu.finex.main.exception.BusinessException;
import hu.finex.main.exception.NotFoundException;
import hu.finex.main.mapper.SavingsAccountMapper;
import hu.finex.main.mapper.SavingsTransactionMapper;
import hu.finex.main.model.Account;
import hu.finex.main.model.SavingsAccount;
import hu.finex.main.model.Transaction;
import hu.finex.main.model.User;
import hu.finex.main.model.enums.AccountStatus;
import hu.finex.main.model.enums.NotificationType;
import hu.finex.main.model.enums.SavingsStatus;
import hu.finex.main.model.enums.SavingsTransactionType;
import hu.finex.main.model.enums.TransactionType;
import hu.finex.main.repository.AccountRepository;
import hu.finex.main.repository.SavingsAccountRepository;
import hu.finex.main.repository.SavingsTransactionRepository;
import hu.finex.main.security.CurrentUser;
import hu.finex.main.util.DateUtils;
import hu.finex.main.util.MoneyUtils;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SavingsAccountService {

    private static final String PARTNER_NAME = "FineX megtakarítás";

	private final SavingsAccountRepository savingsAccountRepository;
    private final SavingsTransactionRepository savingsTransactionRepository;
    private final AccountRepository accountRepository;
    private final SavingsAccountMapper mapper;
    private final SavingsTransactionMapper savingsTransactionMapper;
    private final LedgerService ledgerService;
    private final NotificationService notificationService;
    private final CurrentUser currentUser;
    private final FinexProperties finexProperties;

    // Új megtakarítás: a kamatlábat a bank határozza meg, a kezdő összeg a megadott folyószámláról érkezik
    @Transactional
    public SavingsAccountResponse create(CreateSavingsAccountRequest request) {
        User user = currentUser.requireEntity();

        if (savingsAccountRepository.existsByUser_IdAndNameIgnoreCaseAndStatusNot(user.getId(), request.getName(), SavingsStatus.CLOSED)) {
            throw new BusinessException("Már létezik ilyen nevű megtakarításod.");
        }

        Account current = lockOwnedAccount(request.getAccountId(), user.getId());
        ensureAccountActive(current);

        SavingsAccount savings = mapper.toEntity(request, user, current.getCurrency(), finexProperties.getSavingsInterestRate());
        savings = savingsAccountRepository.save(savings);

        if (request.getInitialDeposit().signum() > 0) {
            moveToSavings(current, savings, request.getInitialDeposit(), "Megtakarítás indítása: " + savings.getName());
        }

        return mapper.toResponse(savings);
    }

    // A bejelentkezett felhasználó nem lezárt megtakarításai
    @Transactional(readOnly = true)
    public List<SavingsAccountResponse> listMine() {
        return savingsAccountRepository.findByUser_IdAndStatusNotOrderByCreatedAtAsc(currentUser.requireId(), SavingsStatus.CLOSED).stream().map(mapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public SavingsAccountResponse getById(Long id) {
        return mapper.toResponse(findOwned(id));
    }

    // Átnevezés és célösszeg módosítása
    @Transactional
    public SavingsAccountResponse update(Long id, UpdateSavingsAccountRequest request) {
        SavingsAccount entity = findOwned(id);

        if (entity.getStatus() == SavingsStatus.CLOSED) {
            throw new BusinessException("Lezárt megtakarítás nem módosítható.");
        }

        if (!entity.getName().equalsIgnoreCase(request.getName())
                && savingsAccountRepository.existsByUser_IdAndNameIgnoreCaseAndStatusNot(entity.getUser().getId(), request.getName(), SavingsStatus.CLOSED)) {
            throw new BusinessException("Ezzel a névvel már létezik megtakarítás.");
        }

        mapper.updateEntity(entity, request);
        return mapper.toResponse(entity);
    }

    // Befizetés a folyószámláról a megtakarításra
    @Transactional
    public SavingsTransferResponse depositFromAccount(Long savingsId, SavingsTransferRequest request) {
        Long userId = currentUser.requireId();
        Account current = lockOwnedAccount(request.getAccountId(), userId);
        SavingsAccount savings = lockOwnedSavings(savingsId, userId);

        ensureAccountActive(current);
        ensureSavingsActive(savings);
        ensureSameCurrency(current, savings);

        String message = request.getMessage() != null ? request.getMessage() : "Megtakarítási betét ide: " + savings.getName();
        Transaction tx = moveToSavings(current, savings, request.getAmount(), message);

        return buildTransferResponse(savings, current, tx);
    }

    // Kivét a megtakarításról a folyószámlára
    @Transactional
    public SavingsTransferResponse withdrawToAccount(Long savingsId, SavingsTransferRequest request) {
        Long userId = currentUser.requireId();
        Account current = lockOwnedAccount(request.getAccountId(), userId);
        SavingsAccount savings = lockOwnedSavings(savingsId, userId);

        ensureAccountActive(current);
        ensureSavingsActive(savings);
        ensureSameCurrency(current, savings);

        String message = request.getMessage() != null ? request.getMessage() : "Megtakarítás kivétele innen: " + savings.getName();
        Transaction tx = moveFromSavings(savings, current, request.getAmount(), message);

        return buildTransferResponse(savings, current, tx);
    }

    // Lezárás: a teljes egyenleg visszakerül a megadott folyószámlára, csak utána lesz CLOSED
    @Transactional
    public void close(Long savingsId, Long accountId) {
        Long userId = currentUser.requireId();
        Account current = lockOwnedAccount(accountId, userId);
        SavingsAccount savings = lockOwnedSavings(savingsId, userId);

        ensureAccountActive(current);
        ensureSavingsActive(savings);
        ensureSameCurrency(current, savings);

        if (savings.getBalance().signum() > 0) {
            moveFromSavings(savings, current, savings.getBalance(), "Megtakarítás lezárása: " + savings.getName());
        }

        savings.setStatus(SavingsStatus.CLOSED);
        savings.setUpdatedAt(Instant.now());
    }

    // A megtakarítás mozgásai (befizetés, kivét, kamat)
    @Transactional(readOnly = true)
    public Page<SavingsTransactionResponse> listTransactions(Long savingsId, Pageable pageable) {
        if (!savingsAccountRepository.existsByIdAndUser_Id(savingsId, currentUser.requireId())) {
            throw new NotFoundException("Megtakarítás nem található.");
        }

        return savingsTransactionRepository.findBySavingsAccount_IdOrderByCreatedAtDesc(savingsId, pageable).map(savingsTransactionMapper::toResponse);
    }

    // A havi kamatjóváírásban részt vevő megtakarítások (az ütemező használja)
    @Transactional(readOnly = true)
    public List<Long> findIdsForInterest() {
        return savingsAccountRepository.findIdsForInterest(SavingsStatus.ACTIVE);
    }

    // Havi kamatjóváírás egy megtakarításra, saját tranzakcióban (az ütemező hívja egyenként).
    // Egyszerű havi kamat: egyenleg * éves kamatláb / 12 / 100, két tizedesre kerekítve. Egy hónapban csak egyszer.
    @Transactional
    public boolean creditMonthlyInterest(Long savingsId) {
        SavingsAccount savings = savingsAccountRepository.findByIdForUpdate(savingsId).orElse(null);

        if (savings == null || savings.getStatus() != SavingsStatus.ACTIVE) {
            return false;
        }
        if (savingsTransactionRepository.existsBySavingsAccount_IdAndTypeAndCreatedAtGreaterThanEqual(savingsId, SavingsTransactionType.INTEREST, DateUtils.startOfCurrentMonth())) {
            return false;
        }

        BigDecimal interest = savings.getBalance()
                .multiply(savings.getInterestRate())
                .divide(BigDecimal.valueOf(1200), 2, RoundingMode.HALF_UP);

        if (interest.signum() <= 0) {
            return false;
        }

        savings.setBalance(savings.getBalance().add(interest));
        savings.setUpdatedAt(Instant.now());
        savingsTransactionRepository.save(savingsTransactionMapper.toEntity(savings, SavingsTransactionType.INTEREST, interest));

        notificationService.notify(savings.getUser(), NotificationType.SAVINGS, "Kamatjóváírás",
                MoneyUtils.format(interest, savings.getCurrency()) + " kamatot írtunk jóvá a(z) " + savings.getName() + " megtakarításodon.");

        return true;
    }

    // Admin: egy felhasználó nem lezárt megtakarításai
    @Transactional(readOnly = true)
    public List<SavingsAccountResponse> listByUser(Long userId) {
        return savingsAccountRepository.findByUser_IdAndStatusNotOrderByCreatedAtAsc(userId, SavingsStatus.CLOSED).stream().map(mapper::toResponse).toList();
    }

    // Folyószámla -> megtakarítás (a folyószámlán OUTCOME tétel, a megtakarításon DEPOSIT)
    private Transaction moveToSavings(Account current, SavingsAccount savings, BigDecimal amount, String message) {
        Transaction tx = ledgerService.debit(current, TransactionType.OUTCOME, amount, PARTNER_NAME, message, null, null);

        savings.setBalance(savings.getBalance().add(amount));
        savings.setUpdatedAt(Instant.now());
        savingsTransactionRepository.save(savingsTransactionMapper.toEntity(savings, SavingsTransactionType.DEPOSIT, amount));

        return tx;
    }

    // Megtakarítás -> folyószámla (a megtakarításon WITHDRAWAL, a folyószámlán INCOME tétel)
    private Transaction moveFromSavings(SavingsAccount savings, Account current, BigDecimal amount, String message) {
        if (savings.getBalance().compareTo(amount) < 0) {
            throw new BusinessException("Nincs elegendő fedezet a megtakarítási számlán.");
        }

        savings.setBalance(savings.getBalance().subtract(amount));
        savings.setUpdatedAt(Instant.now());
        savingsTransactionRepository.save(savingsTransactionMapper.toEntity(savings, SavingsTransactionType.WITHDRAWAL, amount));

        return ledgerService.credit(current, TransactionType.INCOME, amount, PARTNER_NAME, message, null);
    }

    private SavingsTransferResponse buildTransferResponse(SavingsAccount savings, Account current, Transaction tx) {
        return SavingsTransferResponse.builder()
                .savingsAccountId(savings.getId())
                .accountId(current.getId())
                .savingsNewBalance(savings.getBalance())
                .accountNewBalance(current.getBalance())
                .message(tx.getMessage())
                .createdAt(tx.getCreatedAt())
                .build();
    }

    private SavingsAccount findOwned(Long id) {
        return savingsAccountRepository.findByIdAndUser_Id(id, currentUser.requireId()).orElseThrow(() -> new NotFoundException("Megtakarítás nem található."));
    }

    // A zárolási sorrend minden megtakarítás-műveletben ugyanaz: előbb a folyószámla, aztán a megtakarítás
    private Account lockOwnedAccount(Long accountId, Long userId) {
        if (!accountRepository.existsByIdAndUser_Id(accountId, userId)) {
            throw new NotFoundException("A megadott folyószámla nem található.");
        }
        return accountRepository.findByIdForUpdate(accountId).orElseThrow(() -> new NotFoundException("A megadott folyószámla nem található."));
    }

    private SavingsAccount lockOwnedSavings(Long savingsId, Long userId) {
        if (!savingsAccountRepository.existsByIdAndUser_Id(savingsId, userId)) {
            throw new NotFoundException("Megtakarítás nem található.");
        }
        return savingsAccountRepository.findByIdForUpdate(savingsId).orElseThrow(() -> new NotFoundException("Megtakarítás nem található."));
    }

    private void ensureAccountActive(Account account) {
        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new BusinessException("A folyószámla nem aktív, ezért nem végezhető rajta művelet.");
        }
    }

    private void ensureSavingsActive(SavingsAccount savings) {
        if (savings.getStatus() != SavingsStatus.ACTIVE) {
            throw new BusinessException("A megtakarítás nem aktív, ezért nem végezhető rajta művelet.");
        }
    }

    private void ensureSameCurrency(Account account, SavingsAccount savings) {
        if (!savings.getCurrency().equalsIgnoreCase(account.getCurrency())) {
            throw new BusinessException("A számlák devizaneme nem egyezik meg.");
        }
    }
}
