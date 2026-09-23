package hu.finex.main.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hu.finex.main.dto.CreateSavingsAccountRequest;
import hu.finex.main.dto.CreateTransactionRequest;
import hu.finex.main.dto.SavingsAccountResponse;
import hu.finex.main.dto.SavingsTransferRequest;
import hu.finex.main.dto.SavingsTransferResponse;
import hu.finex.main.dto.UpdateSavingsAccountRequest;
import hu.finex.main.exception.BusinessException;
import hu.finex.main.exception.NotFoundException;
import hu.finex.main.mapper.BalanceHistoryMapper;
import hu.finex.main.mapper.SavingsAccountMapper;
import hu.finex.main.mapper.TransactionMapper;
import hu.finex.main.model.Account;
import hu.finex.main.model.SavingsAccount;
import hu.finex.main.model.Transaction;
import hu.finex.main.model.User;
import hu.finex.main.model.enums.AccountType;
import hu.finex.main.model.enums.SavingsStatus;
import hu.finex.main.model.enums.TransactionType;
import hu.finex.main.repository.AccountRepository;
import hu.finex.main.repository.BalanceHistoryRepository;
import hu.finex.main.repository.SavingsAccountRepository;
import hu.finex.main.repository.TransactionRepository;
import hu.finex.main.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SavingsAccountService {

	private final SavingsAccountRepository savingsAccountRepository;
    private final UserRepository userRepository;
    private final SavingsAccountMapper mapper;
    private final AccountRepository accountRepository;
    private final BalanceHistoryRepository balanceHistoryRepository;
    private final BalanceHistoryMapper balanceHistoryMapper;
    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;

    @Transactional
    public SavingsAccountResponse create(CreateSavingsAccountRequest request) {

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new NotFoundException("Felhasználó nem található."));

        if (savingsAccountRepository.existsByUser_IdAndName(request.getUserId(), request.getName())) {
            throw new BusinessException("Már létezik ilyen nevű megtakarítás ennél a felhasználónál.");
        }

        Account current = accountRepository.findFirstByUser_IdAndAccountType(request.getUserId(), AccountType.CURRENT).orElseThrow(() -> new NotFoundException("A felhasználónak nincs folyószámlája."));

        if (!current.getCurrency().equalsIgnoreCase(request.getCurrency())) {
            throw new BusinessException("A kezdő egyenleg devizaneme nem egyezik a folyószámla devizanemével.");
        }

        if (current.getBalance().compareTo(request.getInitialBalance()) < 0) {
            throw new BusinessException("Nincs elegendő fedezet a megtakarítás indításához.");
        }

        current.setBalance(current.getBalance().subtract(request.getInitialBalance()));
        accountRepository.save(current);

        SavingsAccount savings = mapper.toEntity(request, user);
        savings.setStatus(SavingsStatus.ACTIVE);
        savings = savingsAccountRepository.save(savings);

        CreateTransactionRequest txReq = CreateTransactionRequest.builder()
                .accountId(current.getId())
                .type(TransactionType.OUTCOME)
                .amount(request.getInitialBalance())
                .currency(current.getCurrency())
                .message("Megtakarítás indítása: " + savings.getName())
                .fromAccount(current.getAccountNumber())
                .toAccount(null)
                .categoryIds(null)
                .build();

        Transaction tx = transactionRepository.save(
                transactionMapper.toEntity(txReq, current)
        );
        balanceHistoryRepository.save(
                balanceHistoryMapper.toEntity(current, current.getBalance())
        );

        return mapper.toResponse(savings);
    }

    @Transactional(readOnly = true)
    public SavingsAccountResponse getById(Long id) {
        SavingsAccount entity = savingsAccountRepository.findById(id).orElseThrow(() -> new NotFoundException("Megtakarítás nem található."));

        return mapper.toResponse(entity);
    }

    @Transactional(readOnly = true)
    public Page<SavingsAccountResponse> listByUser(Long userId, Pageable pageable) {
        userRepository.findById(userId).orElseThrow(() -> new NotFoundException("Felhasználó nem található."));

        return savingsAccountRepository.findByUser_IdOrderByCreatedAtDesc(userId, pageable).map(mapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<SavingsAccountResponse> listByUserAndStatus(Long userId, SavingsStatus status, Pageable pageable) {
        userRepository.findById(userId).orElseThrow(() -> new NotFoundException("Felhasználó nem található."));

        return savingsAccountRepository.findByUser_IdAndStatusOrderByCreatedAtDesc(userId, status, pageable).map(mapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<SavingsAccountResponse> listAboveBalance(Long userId, java.math.BigDecimal minBalance, Pageable pageable) {
        userRepository.findById(userId).orElseThrow(() -> new NotFoundException("Felhasználó nem található."));

        return savingsAccountRepository.findByUser_IdAndBalanceGreaterThanEqual(userId, minBalance, pageable).map(mapper::toResponse);
    }

    @Transactional
    public SavingsAccountResponse update(Long id, UpdateSavingsAccountRequest request) {

        SavingsAccount entity = savingsAccountRepository.findById(id).orElseThrow(() -> new NotFoundException("Megtakarítás nem található."));

        if (!entity.getName().equalsIgnoreCase(request.getName())&& savingsAccountRepository.existsByUser_IdAndName(entity.getUser().getId(), request.getName())) {
            throw new BusinessException("Ezzel a névvel már létezik megtakarítás.");
        }

        mapper.updateEntity(entity, request);
        return mapper.toResponse(entity);
    }
    
    @Transactional
    public SavingsTransferResponse depositFromAccount(Long savingsId, SavingsTransferRequest request) {
        SavingsAccount savings = savingsAccountRepository.findById(savingsId).orElseThrow(() -> new NotFoundException("Megtakarítási számla nem található: " + savingsId));
        Account current = accountRepository.findById(request.getAccountId()).orElseThrow(() -> new NotFoundException("A megadott folyószámla nem található: " + request.getAccountId()));

        if (!savings.getUser().getId().equals(current.getUser().getId())) {
            throw new BusinessException("A megtakarítási és folyószámla nem ugyanahhoz a felhasználóhoz tartozik.");
        }

        if (!savings.getCurrency().equalsIgnoreCase(current.getCurrency())) {
            throw new BusinessException("A számlák devizaneme nem egyezik meg.");
        }

        if (current.getBalance().compareTo(request.getAmount()) < 0) {
            throw new BusinessException("Nincs elegendő fedezet a folyószámlán.");
        }

        current.setBalance(current.getBalance().subtract(request.getAmount()));
        savings.setBalance(savings.getBalance().add(request.getAmount()));

        accountRepository.save(current);
        savingsAccountRepository.save(savings);

        CreateTransactionRequest txReq = CreateTransactionRequest.builder()
                .accountId(current.getId())
                .type(TransactionType.OUTCOME)
                .amount(request.getAmount())
                .currency(current.getCurrency())
                .message(request.getMessage() != null ? request.getMessage()
                        : "Megtakarítási betét ide: " + savings.getName())
                .fromAccount(current.getAccountNumber())
                .toAccount(null)
                .categoryIds(null)
                .build();

        Transaction tx = transactionMapper.toEntity(txReq, current);
        tx = transactionRepository.save(tx);

        balanceHistoryRepository.save(
                balanceHistoryMapper.toEntity(current, current.getBalance())
        );

        return SavingsTransferResponse.builder()
                .savingsAccountId(savings.getId())
                .accountId(current.getId())
                .savingsNewBalance(savings.getBalance())
                .accountNewBalance(current.getBalance())
                .message(tx.getMessage())
                .createdAt(tx.getCreatedAt())
                .build();
    }

    @Transactional
    public SavingsTransferResponse withdrawToAccount(Long savingsId, SavingsTransferRequest request) {
        SavingsAccount savings = savingsAccountRepository.findById(savingsId).orElseThrow(() -> new NotFoundException("Megtakarítási számla nem található: " + savingsId));
        Account current = accountRepository.findById(request.getAccountId()).orElseThrow(() -> new NotFoundException("A folyószámla nem található: " + request.getAccountId()));

        if (!savings.getUser().getId().equals(current.getUser().getId())) {
            throw new BusinessException("A számlák nem ugyanahhoz a felhasználóhoz tartoznak.");
        }

        if (!savings.getCurrency().equalsIgnoreCase(current.getCurrency())) {
            throw new BusinessException("A számlák devizaneme nem egyezik meg.");
        }

        if (savings.getBalance().compareTo(request.getAmount()) < 0) {
            throw new BusinessException("Nincs elegendő fedezet a megtakarítási számlán.");
        }

        savings.setBalance(savings.getBalance().subtract(request.getAmount()));
        current.setBalance(current.getBalance().add(request.getAmount()));

        savingsAccountRepository.save(savings);
        accountRepository.save(current);

        CreateTransactionRequest txReq = CreateTransactionRequest.builder()
                .accountId(current.getId())
                .type(TransactionType.INCOME)
                .amount(request.getAmount())
                .currency(current.getCurrency())
                .message(request.getMessage() != null ? request.getMessage()
                        : "Megtakarítási kivétele innen: " + savings.getName())
                .fromAccount(null)
                .toAccount(current.getAccountNumber())
                .categoryIds(null)
                .build();

        Transaction tx = transactionMapper.toEntity(txReq, current);
        tx = transactionRepository.save(tx);

        balanceHistoryRepository.save(
                balanceHistoryMapper.toEntity(current, current.getBalance())
        );

        return SavingsTransferResponse.builder()
                .savingsAccountId(savings.getId())
                .accountId(current.getId())
                .savingsNewBalance(savings.getBalance())
                .accountNewBalance(current.getBalance())
                .message(request.getMessage())
                .createdAt(tx.getCreatedAt())
                .build();
    }

    @Transactional
    public void delete(Long id) {
        SavingsAccount entity = savingsAccountRepository.findById(id).orElseThrow(() -> new NotFoundException("Megtakarítás nem található."));

        entity.setStatus(SavingsStatus.CLOSED);
    }
}
