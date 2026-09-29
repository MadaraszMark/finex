package hu.finex.main.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hu.finex.main.config.FinexProperties;
import hu.finex.main.dto.AccountResponse;
import hu.finex.main.dto.CreateAccountRequest;
import hu.finex.main.dto.DepositRequest;
import hu.finex.main.dto.StatementItemResponse;
import hu.finex.main.dto.StatementResponse;
import hu.finex.main.dto.UpdateAccountStatusRequest;
import hu.finex.main.exception.BusinessException;
import hu.finex.main.exception.NotFoundException;
import hu.finex.main.mapper.AccountMapper;
import hu.finex.main.model.Account;
import hu.finex.main.model.User;
import hu.finex.main.model.enums.AccountStatus;
import hu.finex.main.model.enums.AccountType;
import hu.finex.main.model.enums.NotificationType;
import hu.finex.main.model.enums.TransactionType;
import hu.finex.main.repository.AccountRepository;
import hu.finex.main.repository.ReportRepository;
import hu.finex.main.repository.StandingOrderRepository;
import hu.finex.main.security.CurrentUser;
import hu.finex.main.util.DateUtils;
import hu.finex.main.util.IbanUtils;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AccountService {

    private static final Map<AccountStatus, String> STATUS_LABELS = Map.of(
            AccountStatus.ACTIVE, "aktív",
            AccountStatus.BLOCKED, "letiltott",
            AccountStatus.FROZEN, "befagyasztott",
            AccountStatus.CLOSED, "lezárt");

    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;
    private final CardService cardService;
    private final LedgerService ledgerService;
    private final NotificationService notificationService;
    private final StandingOrderRepository standingOrderRepository;
    private final ReportRepository reportRepository;
    private final CurrentUser currentUser;
    private final FinexProperties finexProperties;

    // A bejelentkezett felhasználó összes számlája nyitás sorrendjében
    @Transactional(readOnly = true)
    public List<AccountResponse> listMine() {
        return accountRepository.findByUser_IdOrderByCreatedAtAsc(currentUser.requireId()).stream().map(accountMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public AccountResponse getById(Long id) {
        return accountMapper.toResponse(findOwned(id));
    }

    // Elsődleges folyószámla: a legrégebbi aktív folyószámla (a főoldal kártyájához)
    @Transactional(readOnly = true)
    public AccountResponse getMyAccount() {
        Account account = accountRepository
                .findFirstByUser_IdAndStatusAndAccountTypeOrderByCreatedAtAsc(currentUser.requireId(), AccountStatus.ACTIVE, AccountType.CURRENT)
                .orElseThrow(() -> new NotFoundException("Számla nem található."));

        return accountMapper.toResponse(account);
    }

    // Új folyószámla nyitása a bejelentkezett felhasználónak, bankkártyával együtt
    @Transactional
    public AccountResponse open(CreateAccountRequest request) {
        User user = currentUser.requireEntity();

        if (accountRepository.countByUser_IdAndStatusNot(user.getId(), AccountStatus.CLOSED) >= finexProperties.getMaxAccountsPerUser()) {
            throw new BusinessException("Legfeljebb " + finexProperties.getMaxAccountsPerUser() + " nyitott számlád lehet.");
        }

        Account account = accountMapper.toEntity(request, user, generateUniqueAccountNumber());
        account = accountRepository.save(account);

        cardService.createCard(account);

        return accountMapper.toResponse(account);
    }

    // Regisztrációkor automatikusan létrejövő forint folyószámla, bankkártyával
    @Transactional
    public Account createDefaultAccount(User user) {
        Account account = Account.builder()
                .user(user)
                .name("Fő számla")
                .accountNumber(generateUniqueAccountNumber())
                .balance(BigDecimal.ZERO)
                .currency("HUF")
                .accountType(AccountType.CURRENT)
                .status(AccountStatus.ACTIVE)
                .build();
        account = accountRepository.save(account);

        cardService.createCard(account);

        return account;
    }

    // Befizetés (demó: pl. ATM-es készpénzbefizetés)
    @Transactional
    public AccountResponse deposit(Long accountId, DepositRequest request) {
        Account account = lockOwned(accountId);
        ensureActive(account);

        String message = request.getMessage() != null ? request.getMessage() : "Készpénzbefizetés";
        ledgerService.credit(account, TransactionType.INCOME, request.getAmount(), "Készpénzbefizetés", message, null);

        return accountMapper.toResponse(account);
    }

    // Számla lezárása: csak nulla egyenleggel, és az utolsó nyitott számla nem zárható le.
    // A kártyák megszűnnek, a számláról induló rendszeres átutalások leállnak.
    @Transactional
    public void close(Long id) {
        Account account = lockOwned(id);

        if (account.getStatus() == AccountStatus.CLOSED) {
            throw new BusinessException("A számla már le van zárva.");
        }
        if (account.getBalance().signum() != 0) {
            throw new BusinessException("Csak nulla egyenlegű számla zárható le. Előbb utald át a pénzt egy másik számládra.");
        }
        if (accountRepository.countByUser_IdAndStatusNot(account.getUser().getId(), AccountStatus.CLOSED) <= 1) {
            throw new BusinessException("Az utolsó nyitott számládat nem zárhatod le.");
        }

        closeAccount(account);
    }

    // Számlakivonat: a nyitóegyenleget és a futó egyenleget az adatbázis függvényei számolják
    // (account_balance_at, account_statement), itt csak az összesítők készülnek
    @Transactional(readOnly = true)
    public StatementResponse getStatement(Long id, LocalDate from, LocalDate to) {
        Account account = findOwned(id);

        if (from.isAfter(to)) {
            throw new BusinessException("A kezdő dátum nem lehet későbbi a záró dátumnál.");
        }

        Instant fromInstant = DateUtils.startOfDay(from);
        Instant toInstant = DateUtils.startOfDay(to.plusDays(1));

        BigDecimal openingBalance = reportRepository.findBalanceAt(account.getId(), fromInstant);
        List<StatementItemResponse> items = reportRepository.findStatement(account.getId(), fromInstant, toInstant);

        BigDecimal totalIncome = items.stream()
                .map(StatementItemResponse::getSignedAmount)
                .filter(amount -> amount.signum() > 0)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalOutcome = items.stream()
                .map(StatementItemResponse::getSignedAmount)
                .filter(amount -> amount.signum() < 0)
                .map(BigDecimal::negate)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal closingBalance = items.isEmpty() ? openingBalance : items.get(items.size() - 1).getRunningBalance();

        return StatementResponse.builder()
                .accountId(account.getId())
                .accountNumber(account.getAccountNumber())
                .currency(account.getCurrency())
                .from(from)
                .to(to)
                .openingBalance(openingBalance)
                .closingBalance(closingBalance)
                .totalIncome(totalIncome)
                .totalOutcome(totalOutcome)
                .items(items)
                .build();
    }

    // Admin: összes számla, opcionálisan státusz szerint
    @Transactional(readOnly = true)
    public Page<AccountResponse> listAll(AccountStatus status, Pageable pageable) {
        Page<Account> accounts = status != null ? accountRepository.findByStatus(status, pageable) : accountRepository.findAll(pageable);
        return accounts.map(accountMapper::toResponse);
    }

    // Admin: számla befagyasztása, tiltása, feloldása vagy lezárása; a tulajdonos értesítést kap
    @Transactional
    public AccountResponse updateStatus(Long id, UpdateAccountStatusRequest request) {
        Account account = accountRepository.findByIdForUpdate(id).orElseThrow(() -> new NotFoundException("Számla nem található."));

        if (account.getStatus() == AccountStatus.CLOSED) {
            throw new BusinessException("Lezárt számla státusza nem módosítható.");
        }

        if (request.getStatus() == AccountStatus.CLOSED) {
            if (account.getBalance().signum() != 0) {
                throw new BusinessException("Csak nulla egyenlegű számla zárható le.");
            }
            closeAccount(account);
        } else {
            accountMapper.updateStatus(account, request);
        }

        notificationService.notify(account.getUser(), NotificationType.SECURITY, "Számlád státusza megváltozott",
                "A(z) " + account.getName() + " számlád új státusza: " + STATUS_LABELS.get(account.getStatus()) + ".");

        return accountMapper.toResponse(account);
    }

    private void closeAccount(Account account) {
        account.setStatus(AccountStatus.CLOSED);
        cardService.cancelCardsOfAccount(account.getId());
        standingOrderRepository.findByAccount_IdAndActiveTrue(account.getId()).forEach(order -> order.setActive(false));
    }

    private Account findOwned(Long id) {
        return accountRepository.findByIdAndUser_Id(id, currentUser.requireId()).orElseThrow(() -> new NotFoundException("Számla nem található."));
    }

    // Először csak ellenőrzünk (entitás betöltése nélkül), aztán zárolva olvassuk be: így biztosan a legfrissebb egyenleggel számolunk
    private Account lockOwned(Long id) {
        if (!accountRepository.existsByIdAndUser_Id(id, currentUser.requireId())) {
            throw new NotFoundException("Számla nem található.");
        }
        return accountRepository.findByIdForUpdate(id).orElseThrow(() -> new NotFoundException("Számla nem található."));
    }

    private void ensureActive(Account account) {
        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new BusinessException("A számla nem aktív, ezért nem végezhető rajta művelet.");
        }
    }

    // Egyedi IBAN számlaszám (ütközés esetén újrapróbálkozás)
    private String generateUniqueAccountNumber() {
        String accountNumber;
        do {
            accountNumber = IbanUtils.generateHungarian();
        } while (accountRepository.existsByAccountNumber(accountNumber));
        return accountNumber;
    }
}
