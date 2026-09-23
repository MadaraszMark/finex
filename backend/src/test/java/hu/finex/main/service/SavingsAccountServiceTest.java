package hu.finex.main.service;

import hu.finex.main.dto.*;
import hu.finex.main.exception.BusinessException;
import hu.finex.main.exception.NotFoundException;
import hu.finex.main.mapper.BalanceHistoryMapper;
import hu.finex.main.mapper.SavingsAccountMapper;
import hu.finex.main.mapper.TransactionMapper;
import hu.finex.main.model.Account;
import hu.finex.main.model.BalanceHistory;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SavingsAccountServiceTest {

    @Mock private SavingsAccountRepository savingsAccountRepository;
    @Mock private UserRepository userRepository;
    @Mock private SavingsAccountMapper mapper;
    @Mock private AccountRepository accountRepository;
    @Mock private BalanceHistoryRepository balanceHistoryRepository;
    @Mock private BalanceHistoryMapper balanceHistoryMapper;
    @Mock private TransactionRepository transactionRepository;
    @Mock private TransactionMapper transactionMapper;

    @InjectMocks private SavingsAccountService service;

    @Test
    void create_shouldThrowNotFound_whenUserMissing() {
        CreateSavingsAccountRequest req = CreateSavingsAccountRequest.builder()
                .userId(1L)
                .name("S1")
                .currency("HUF")
                .initialBalance(new BigDecimal("100.00"))
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.create(req));

        verify(userRepository).findById(1L);
        verifyNoInteractions(savingsAccountRepository, accountRepository, mapper, transactionRepository, balanceHistoryRepository);
    }

    @Test
    void create_shouldThrowBusinessException_whenDuplicateNameForUser() {
        CreateSavingsAccountRequest req = CreateSavingsAccountRequest.builder()
                .userId(1L)
                .name("S1")
                .currency("HUF")
                .initialBalance(new BigDecimal("100.00"))
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(User.builder().id(1L).build()));
        when(savingsAccountRepository.existsByUser_IdAndName(1L, "S1")).thenReturn(true);

        assertThrows(BusinessException.class, () -> service.create(req));

        verify(savingsAccountRepository).existsByUser_IdAndName(1L, "S1");
        verifyNoInteractions(accountRepository, mapper, transactionRepository, balanceHistoryRepository);
    }

    @Test
    void create_shouldThrowNotFound_whenNoCurrentAccount() {
        CreateSavingsAccountRequest req = CreateSavingsAccountRequest.builder()
                .userId(1L)
                .name("S1")
                .currency("HUF")
                .initialBalance(new BigDecimal("100.00"))
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(User.builder().id(1L).build()));
        when(savingsAccountRepository.existsByUser_IdAndName(1L, "S1")).thenReturn(false);
        when(accountRepository.findFirstByUser_IdAndAccountType(1L, AccountType.CURRENT)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.create(req));

        verify(accountRepository).findFirstByUser_IdAndAccountType(1L, AccountType.CURRENT);
        verifyNoInteractions(mapper, transactionRepository, balanceHistoryRepository);
    }

    @Test
    void create_shouldThrowBusinessException_whenCurrencyMismatch() {
        CreateSavingsAccountRequest req = CreateSavingsAccountRequest.builder()
                .userId(1L)
                .name("S1")
                .currency("EUR")
                .initialBalance(new BigDecimal("100.00"))
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(User.builder().id(1L).build()));
        when(savingsAccountRepository.existsByUser_IdAndName(1L, "S1")).thenReturn(false);

        Account current = Account.builder()
                .id(10L)
                .currency("HUF")
                .balance(new BigDecimal("500.00"))
                .build();
        when(accountRepository.findFirstByUser_IdAndAccountType(1L, AccountType.CURRENT)).thenReturn(Optional.of(current));

        assertThrows(BusinessException.class, () -> service.create(req));

        verifyNoInteractions(mapper, transactionRepository, balanceHistoryRepository);
        verify(accountRepository, never()).save(any());
        verify(savingsAccountRepository, never()).save(any());
    }

    @Test
    void create_shouldThrowBusinessException_whenInsufficientFunds() {
        CreateSavingsAccountRequest req = CreateSavingsAccountRequest.builder()
                .userId(1L)
                .name("S1")
                .currency("HUF")
                .initialBalance(new BigDecimal("600.00"))
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(User.builder().id(1L).build()));
        when(savingsAccountRepository.existsByUser_IdAndName(1L, "S1")).thenReturn(false);

        Account current = Account.builder()
                .id(10L)
                .currency("HUF")
                .balance(new BigDecimal("500.00"))
                .build();
        when(accountRepository.findFirstByUser_IdAndAccountType(1L, AccountType.CURRENT)).thenReturn(Optional.of(current));

        assertThrows(BusinessException.class, () -> service.create(req));

        verify(accountRepository, never()).save(any());
        verify(savingsAccountRepository, never()).save(any());
        verifyNoInteractions(mapper, transactionRepository, balanceHistoryRepository);
    }

    @Test
    void create_shouldDeductFromCurrent_createSavings_saveTxAndBalanceHistory_andReturnResponse() {
        CreateSavingsAccountRequest req = CreateSavingsAccountRequest.builder()
                .userId(1L)
                .name("S1")
                .currency("HUF")
                .initialBalance(new BigDecimal("100.00"))
                .build();

        User user = User.builder().id(1L).build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(savingsAccountRepository.existsByUser_IdAndName(1L, "S1")).thenReturn(false);

        Account current = Account.builder()
                .id(10L)
                .user(user)
                .accountNumber("CURR-ACC")
                .currency("HUF")
                .balance(new BigDecimal("500.00"))
                .build();
        when(accountRepository.findFirstByUser_IdAndAccountType(1L, AccountType.CURRENT)).thenReturn(Optional.of(current));

        when(accountRepository.save(current)).thenAnswer(inv -> inv.getArgument(0));

        SavingsAccount mappedSavings = SavingsAccount.builder()
                .user(user)
                .name("S1")
                .balance(req.getInitialBalance())
                .currency("HUF")
                .status(null)
                .build();
        when(mapper.toEntity(req, user)).thenReturn(mappedSavings);

        SavingsAccount savedSavings = SavingsAccount.builder()
                .id(99L)
                .user(user)
                .name("S1")
                .balance(req.getInitialBalance())
                .currency("HUF")
                .status(SavingsStatus.ACTIVE)
                .build();
        when(savingsAccountRepository.save(any(SavingsAccount.class))).thenReturn(savedSavings);

        when(transactionMapper.toEntity(any(CreateTransactionRequest.class), eq(current)))
                .thenReturn(Transaction.builder().account(current).build());

        Transaction savedTx = Transaction.builder()
                .id(77L)
                .account(current)
                .message("x")
                .createdAt(Instant.now())
                .build();
        when(transactionRepository.save(any(Transaction.class))).thenReturn(savedTx);

        when(balanceHistoryMapper.toEntity(eq(current), any(BigDecimal.class)))
                .thenReturn(BalanceHistory.builder().account(current).build());
        when(balanceHistoryRepository.save(any(BalanceHistory.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        SavingsAccountResponse expectedResp = SavingsAccountResponse.builder()
                .id(99L)
                .userId(1L)
                .name("S1")
                .status(SavingsStatus.ACTIVE)
                .build();
        when(mapper.toResponse(savedSavings)).thenReturn(expectedResp);

        SavingsAccountResponse resp = service.create(req);

        assertNotNull(resp);
        assertEquals(99L, resp.getId());
        assertEquals("S1", resp.getName());
        assertEquals(SavingsStatus.ACTIVE, resp.getStatus());
        assertEquals(new BigDecimal("400.00"), current.getBalance());

        ArgumentCaptor<CreateTransactionRequest> txReqCaptor = ArgumentCaptor.forClass(CreateTransactionRequest.class);
        verify(transactionMapper).toEntity(txReqCaptor.capture(), eq(current));
        CreateTransactionRequest capturedTxReq = txReqCaptor.getValue();
        assertEquals(current.getId(), capturedTxReq.getAccountId());
        assertEquals(TransactionType.OUTCOME, capturedTxReq.getType());
        assertEquals(req.getInitialBalance(), capturedTxReq.getAmount());
        assertEquals(current.getCurrency(), capturedTxReq.getCurrency());
        assertEquals(current.getAccountNumber(), capturedTxReq.getFromAccount());

        verify(balanceHistoryMapper).toEntity(eq(current), eq(new BigDecimal("400.00")));
        verify(balanceHistoryRepository).save(any(BalanceHistory.class));
        verify(transactionRepository).save(any(Transaction.class));
        verify(mapper).toResponse(savedSavings);
    }

    @Test
    void getById_shouldReturnResponse() {
        SavingsAccount entity = SavingsAccount.builder().id(5L).build();
        when(savingsAccountRepository.findById(5L)).thenReturn(Optional.of(entity));

        SavingsAccountResponse expected = SavingsAccountResponse.builder().id(5L).build();
        when(mapper.toResponse(entity)).thenReturn(expected);

        SavingsAccountResponse resp = service.getById(5L);

        assertNotNull(resp);
        assertEquals(5L, resp.getId());

        verify(savingsAccountRepository).findById(5L);
        verify(mapper).toResponse(entity);
    }

    @Test
    void getById_shouldThrowNotFound_whenMissing() {
        when(savingsAccountRepository.findById(5L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.getById(5L));

        verify(savingsAccountRepository).findById(5L);
        verifyNoInteractions(mapper);
    }

    @Test
    void listByUser_shouldThrowNotFound_whenUserMissing() {
        Pageable pageable = PageRequest.of(0, 10);
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.listByUser(1L, pageable));

        verify(userRepository).findById(1L);
        verifyNoInteractions(savingsAccountRepository, mapper);
    }

    @Test
    void listByUser_shouldReturnMappedPage() {
        Pageable pageable = PageRequest.of(0, 2);
        when(userRepository.findById(1L)).thenReturn(Optional.of(User.builder().id(1L).build()));

        SavingsAccount s1 = SavingsAccount.builder().id(1L).build();
        SavingsAccount s2 = SavingsAccount.builder().id(2L).build();
        Page<SavingsAccount> page = new PageImpl<>(List.of(s1, s2), pageable, 2);

        when(savingsAccountRepository.findByUser_IdOrderByCreatedAtDesc(1L, pageable)).thenReturn(page);

        SavingsAccountResponse r1 = SavingsAccountResponse.builder().id(1L).build();
        SavingsAccountResponse r2 = SavingsAccountResponse.builder().id(2L).build();
        when(mapper.toResponse(s1)).thenReturn(r1);
        when(mapper.toResponse(s2)).thenReturn(r2);

        Page<SavingsAccountResponse> resp = service.listByUser(1L, pageable);

        assertEquals(2, resp.getTotalElements());
        assertSame(r1, resp.getContent().get(0));
        assertSame(r2, resp.getContent().get(1));

        verify(savingsAccountRepository).findByUser_IdOrderByCreatedAtDesc(1L, pageable);
        verify(mapper).toResponse(s1);
        verify(mapper).toResponse(s2);
    }

    @Test
    void update_shouldThrowNotFound_whenSavingsMissing() {
        UpdateSavingsAccountRequest req = UpdateSavingsAccountRequest.builder()
                .name("X")
                .status(SavingsStatus.ACTIVE)
                .build();

        when(savingsAccountRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.update(1L, req));

        verify(savingsAccountRepository).findById(1L);
        verifyNoInteractions(mapper);
    }

    @Test
    void update_shouldThrowBusinessException_whenRenamingToExistingName() {
        User user = User.builder().id(1L).build();
        SavingsAccount entity = SavingsAccount.builder()
                .id(10L)
                .user(user)
                .name("OLD")
                .build();

        UpdateSavingsAccountRequest req = UpdateSavingsAccountRequest.builder()
                .name("NEW")
                .status(SavingsStatus.ACTIVE)
                .build();

        when(savingsAccountRepository.findById(10L)).thenReturn(Optional.of(entity));
        when(savingsAccountRepository.existsByUser_IdAndName(1L, "NEW")).thenReturn(true);

        assertThrows(BusinessException.class, () -> service.update(10L, req));

        verify(savingsAccountRepository).findById(10L);
        verify(savingsAccountRepository).existsByUser_IdAndName(1L, "NEW");
        verify(mapper, never()).updateEntity(any(), any());
    }

    @Test
    void update_shouldUpdateAndReturnResponse_whenOk() {
        User user = User.builder().id(1L).build();
        SavingsAccount entity = SavingsAccount.builder()
                .id(10L)
                .user(user)
                .name("OLD")
                .build();

        UpdateSavingsAccountRequest req = UpdateSavingsAccountRequest.builder()
                .name("OLD")
                .interestRate(new BigDecimal("3.00"))
                .status(SavingsStatus.FROZEN)
                .build();

        when(savingsAccountRepository.findById(10L)).thenReturn(Optional.of(entity));

        SavingsAccountResponse expected = SavingsAccountResponse.builder().id(10L).build();
        when(mapper.toResponse(entity)).thenReturn(expected);

        SavingsAccountResponse resp = service.update(10L, req);

        assertNotNull(resp);
        assertEquals(10L, resp.getId());

        verify(mapper).updateEntity(entity, req);
        verify(mapper).toResponse(entity);
    }

    @Test
    void depositFromAccount_shouldThrowNotFound_whenSavingsMissing() {
        SavingsTransferRequest req = SavingsTransferRequest.builder()
                .accountId(1L)
                .amount(new BigDecimal("10.00"))
                .build();

        when(savingsAccountRepository.findById(5L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.depositFromAccount(5L, req));

        verify(savingsAccountRepository).findById(5L);
        verifyNoInteractions(accountRepository, transactionRepository, balanceHistoryRepository);
    }

    @Test
    void depositFromAccount_shouldThrowBusinessException_whenDifferentUsers() {
        User u1 = User.builder().id(1L).build();
        User u2 = User.builder().id(2L).build();

        SavingsAccount savings = SavingsAccount.builder()
                .id(5L).user(u1).currency("HUF").balance(BigDecimal.ZERO).name("S")
                .build();
        Account current = Account.builder()
                .id(10L).user(u2).currency("HUF").balance(new BigDecimal("100.00")).accountNumber("CURR")
                .build();

        when(savingsAccountRepository.findById(5L)).thenReturn(Optional.of(savings));
        when(accountRepository.findById(10L)).thenReturn(Optional.of(current));

        SavingsTransferRequest req = SavingsTransferRequest.builder()
                .accountId(10L)
                .amount(new BigDecimal("10.00"))
                .build();

        assertThrows(BusinessException.class, () -> service.depositFromAccount(5L, req));

        verify(accountRepository, never()).save(any());
        verify(savingsAccountRepository, never()).save(any());
        verifyNoInteractions(transactionRepository, balanceHistoryRepository);
    }

    @Test
    void depositFromAccount_shouldThrowBusinessException_whenCurrencyMismatch() {
        User u1 = User.builder().id(1L).build();

        SavingsAccount savings = SavingsAccount.builder()
                .id(5L).user(u1).currency("EUR").balance(BigDecimal.ZERO).name("S")
                .build();
        Account current = Account.builder()
                .id(10L).user(u1).currency("HUF").balance(new BigDecimal("100.00")).accountNumber("CURR")
                .build();

        when(savingsAccountRepository.findById(5L)).thenReturn(Optional.of(savings));
        when(accountRepository.findById(10L)).thenReturn(Optional.of(current));

        SavingsTransferRequest req = SavingsTransferRequest.builder()
                .accountId(10L)
                .amount(new BigDecimal("10.00"))
                .build();

        assertThrows(BusinessException.class, () -> service.depositFromAccount(5L, req));

        verify(accountRepository, never()).save(any());
        verify(savingsAccountRepository, never()).save(any());
        verifyNoInteractions(transactionRepository, balanceHistoryRepository);
    }

    @Test
    void depositFromAccount_shouldThrowBusinessException_whenInsufficientFunds() {
        User u1 = User.builder().id(1L).build();

        SavingsAccount savings = SavingsAccount.builder()
                .id(5L).user(u1).currency("HUF").balance(BigDecimal.ZERO).name("S")
                .build();
        Account current = Account.builder()
                .id(10L).user(u1).currency("HUF").balance(new BigDecimal("5.00")).accountNumber("CURR")
                .build();

        when(savingsAccountRepository.findById(5L)).thenReturn(Optional.of(savings));
        when(accountRepository.findById(10L)).thenReturn(Optional.of(current));

        SavingsTransferRequest req = SavingsTransferRequest.builder()
                .accountId(10L)
                .amount(new BigDecimal("10.00"))
                .build();

        assertThrows(BusinessException.class, () -> service.depositFromAccount(5L, req));

        verify(accountRepository, never()).save(any());
        verify(savingsAccountRepository, never()).save(any());
        verifyNoInteractions(transactionRepository, balanceHistoryRepository);
    }

    @Test
    void depositFromAccount_shouldMoveMoney_saveTxAndBalanceHistory_andReturnResponse() {
        User user = User.builder().id(1L).build();

        SavingsAccount savings = SavingsAccount.builder()
                .id(5L)
                .user(user)
                .currency("HUF")
                .balance(new BigDecimal("100.00"))
                .name("SAV")
                .build();

        Account current = Account.builder()
                .id(10L)
                .user(user)
                .currency("HUF")
                .balance(new BigDecimal("500.00"))
                .accountNumber("CURR-ACC")
                .build();

        when(savingsAccountRepository.findById(5L)).thenReturn(Optional.of(savings));
        when(accountRepository.findById(10L)).thenReturn(Optional.of(current));

        when(accountRepository.save(current)).thenAnswer(inv -> inv.getArgument(0));
        when(savingsAccountRepository.save(savings)).thenAnswer(inv -> inv.getArgument(0));

        Transaction txEntity = Transaction.builder().account(current).build();
        when(transactionMapper.toEntity(any(CreateTransactionRequest.class), eq(current))).thenReturn(txEntity);

        Transaction savedTx = Transaction.builder()
                .id(77L)
                .account(current)
                .message("DEFAULT")
                .createdAt(Instant.parse("2025-01-01T10:00:00Z"))
                .build();
        when(transactionRepository.save(txEntity)).thenReturn(savedTx);

        when(balanceHistoryMapper.toEntity(eq(current), any(BigDecimal.class)))
                .thenReturn(BalanceHistory.builder().account(current).build());
        when(balanceHistoryRepository.save(any(BalanceHistory.class))).thenAnswer(inv -> inv.getArgument(0));

        SavingsTransferRequest req = SavingsTransferRequest.builder()
                .accountId(10L)
                .amount(new BigDecimal("50.00"))
                .message(null)
                .build();

        SavingsTransferResponse resp = service.depositFromAccount(5L, req);

        assertNotNull(resp);
        assertEquals(5L, resp.getSavingsAccountId());
        assertEquals(10L, resp.getAccountId());
        assertEquals(new BigDecimal("150.00"), resp.getSavingsNewBalance());
        assertEquals(new BigDecimal("450.00"), resp.getAccountNewBalance());
        assertEquals(savedTx.getMessage(), resp.getMessage());
        assertEquals(savedTx.getCreatedAt(), resp.getCreatedAt());

        ArgumentCaptor<CreateTransactionRequest> txReqCaptor = ArgumentCaptor.forClass(CreateTransactionRequest.class);
        verify(transactionMapper).toEntity(txReqCaptor.capture(), eq(current));
        CreateTransactionRequest capturedTxReq = txReqCaptor.getValue();
        assertEquals(TransactionType.OUTCOME, capturedTxReq.getType());
        assertEquals(new BigDecimal("50.00"), capturedTxReq.getAmount());
        assertEquals("HUF", capturedTxReq.getCurrency());
        assertEquals("CURR-ACC", capturedTxReq.getFromAccount());

        verify(balanceHistoryMapper).toEntity(eq(current), eq(new BigDecimal("450.00")));
        verify(balanceHistoryRepository).save(any(BalanceHistory.class));
    }

    @Test
    void withdrawToAccount_shouldThrowNotFound_whenSavingsMissing() {
        SavingsTransferRequest req = SavingsTransferRequest.builder()
                .accountId(1L)
                .amount(new BigDecimal("10.00"))
                .build();

        when(savingsAccountRepository.findById(5L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.withdrawToAccount(5L, req));

        verify(savingsAccountRepository).findById(5L);
        verifyNoInteractions(accountRepository, transactionRepository, balanceHistoryRepository);
    }

    @Test
    void withdrawToAccount_shouldThrowBusinessException_whenDifferentUsers() {
        User u1 = User.builder().id(1L).build();
        User u2 = User.builder().id(2L).build();

        SavingsAccount savings = SavingsAccount.builder()
                .id(5L).user(u1).currency("HUF").balance(new BigDecimal("100.00")).name("S")
                .build();
        Account current = Account.builder()
                .id(10L).user(u2).currency("HUF").balance(new BigDecimal("0.00")).accountNumber("CURR")
                .build();

        when(savingsAccountRepository.findById(5L)).thenReturn(Optional.of(savings));
        when(accountRepository.findById(10L)).thenReturn(Optional.of(current));

        SavingsTransferRequest req = SavingsTransferRequest.builder()
                .accountId(10L)
                .amount(new BigDecimal("10.00"))
                .build();

        assertThrows(BusinessException.class, () -> service.withdrawToAccount(5L, req));

        verify(accountRepository, never()).save(any());
        verify(savingsAccountRepository, never()).save(any());
        verifyNoInteractions(transactionRepository, balanceHistoryRepository);
    }

    @Test
    void withdrawToAccount_shouldThrowBusinessException_whenCurrencyMismatch() {
        User u1 = User.builder().id(1L).build();

        SavingsAccount savings = SavingsAccount.builder()
                .id(5L).user(u1).currency("EUR").balance(new BigDecimal("100.00")).name("S")
                .build();
        Account current = Account.builder()
                .id(10L).user(u1).currency("HUF").balance(new BigDecimal("0.00")).accountNumber("CURR")
                .build();

        when(savingsAccountRepository.findById(5L)).thenReturn(Optional.of(savings));
        when(accountRepository.findById(10L)).thenReturn(Optional.of(current));

        SavingsTransferRequest req = SavingsTransferRequest.builder()
                .accountId(10L)
                .amount(new BigDecimal("10.00"))
                .build();

        assertThrows(BusinessException.class, () -> service.withdrawToAccount(5L, req));

        verify(accountRepository, never()).save(any());
        verify(savingsAccountRepository, never()).save(any());
        verifyNoInteractions(transactionRepository, balanceHistoryRepository);
    }

    @Test
    void withdrawToAccount_shouldThrowBusinessException_whenInsufficientSavingsBalance() {
        User u1 = User.builder().id(1L).build();

        SavingsAccount savings = SavingsAccount.builder()
                .id(5L).user(u1).currency("HUF").balance(new BigDecimal("5.00")).name("S")
                .build();
        Account current = Account.builder()
                .id(10L).user(u1).currency("HUF").balance(new BigDecimal("0.00")).accountNumber("CURR")
                .build();

        when(savingsAccountRepository.findById(5L)).thenReturn(Optional.of(savings));
        when(accountRepository.findById(10L)).thenReturn(Optional.of(current));

        SavingsTransferRequest req = SavingsTransferRequest.builder()
                .accountId(10L)
                .amount(new BigDecimal("10.00"))
                .build();

        assertThrows(BusinessException.class, () -> service.withdrawToAccount(5L, req));

        verify(accountRepository, never()).save(any());
        verify(savingsAccountRepository, never()).save(any());
        verifyNoInteractions(transactionRepository, balanceHistoryRepository);
    }

    @Test
    void withdrawToAccount_shouldMoveMoney_saveTxAndBalanceHistory_andReturnResponse() {
        User user = User.builder().id(1L).build();

        SavingsAccount savings = SavingsAccount.builder()
                .id(5L)
                .user(user)
                .currency("HUF")
                .balance(new BigDecimal("200.00"))
                .name("SAV")
                .build();

        Account current = Account.builder()
                .id(10L)
                .user(user)
                .currency("HUF")
                .balance(new BigDecimal("500.00"))
                .accountNumber("CURR-ACC")
                .build();

        when(savingsAccountRepository.findById(5L)).thenReturn(Optional.of(savings));
        when(accountRepository.findById(10L)).thenReturn(Optional.of(current));

        when(savingsAccountRepository.save(savings)).thenAnswer(inv -> inv.getArgument(0));
        when(accountRepository.save(current)).thenAnswer(inv -> inv.getArgument(0));

        Transaction txEntity = Transaction.builder().account(current).build();
        when(transactionMapper.toEntity(any(CreateTransactionRequest.class), eq(current))).thenReturn(txEntity);

        Transaction savedTx = Transaction.builder()
                .id(77L)
                .account(current)
                .message("DEFAULT")
                .createdAt(Instant.parse("2025-01-01T10:00:00Z"))
                .build();
        when(transactionRepository.save(txEntity)).thenReturn(savedTx);

        when(balanceHistoryMapper.toEntity(eq(current), any(BigDecimal.class)))
                .thenReturn(BalanceHistory.builder().account(current).build());
        when(balanceHistoryRepository.save(any(BalanceHistory.class))).thenAnswer(inv -> inv.getArgument(0));

        SavingsTransferRequest req = SavingsTransferRequest.builder()
                .accountId(10L)
                .amount(new BigDecimal("50.00"))
                .message(null)
                .build();

        SavingsTransferResponse resp = service.withdrawToAccount(5L, req);

        assertNotNull(resp);
        assertEquals(5L, resp.getSavingsAccountId());
        assertEquals(10L, resp.getAccountId());
        assertEquals(new BigDecimal("150.00"), resp.getSavingsNewBalance());
        assertEquals(new BigDecimal("550.00"), resp.getAccountNewBalance());
        assertNull(resp.getMessage());
        assertEquals(savedTx.getCreatedAt(), resp.getCreatedAt());

        ArgumentCaptor<CreateTransactionRequest> txReqCaptor = ArgumentCaptor.forClass(CreateTransactionRequest.class);
        verify(transactionMapper).toEntity(txReqCaptor.capture(), eq(current));
        CreateTransactionRequest capturedTxReq = txReqCaptor.getValue();
        assertEquals(TransactionType.INCOME, capturedTxReq.getType());
        assertEquals(new BigDecimal("50.00"), capturedTxReq.getAmount());
        assertEquals("HUF", capturedTxReq.getCurrency());
        assertEquals("CURR-ACC", capturedTxReq.getToAccount());

        verify(balanceHistoryMapper).toEntity(eq(current), eq(new BigDecimal("550.00")));
        verify(balanceHistoryRepository).save(any(BalanceHistory.class));
    }

    @Test
    void delete_shouldSetStatusClosed() {
        SavingsAccount entity = SavingsAccount.builder()
                .id(7L)
                .status(SavingsStatus.ACTIVE)
                .build();

        when(savingsAccountRepository.findById(7L)).thenReturn(Optional.of(entity));

        service.delete(7L);

        assertEquals(SavingsStatus.CLOSED, entity.getStatus());
        verify(savingsAccountRepository).findById(7L);
    }
}
