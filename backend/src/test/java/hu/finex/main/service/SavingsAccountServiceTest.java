package hu.finex.main.service;

import hu.finex.main.config.FinexProperties;
import hu.finex.main.dto.*;
import hu.finex.main.exception.BusinessException;
import hu.finex.main.exception.NotFoundException;
import hu.finex.main.mapper.SavingsAccountMapper;
import hu.finex.main.mapper.SavingsTransactionMapper;
import hu.finex.main.model.*;
import hu.finex.main.model.enums.AccountStatus;
import hu.finex.main.model.enums.NotificationType;
import hu.finex.main.model.enums.SavingsStatus;
import hu.finex.main.model.enums.SavingsTransactionType;
import hu.finex.main.model.enums.TransactionType;
import hu.finex.main.repository.AccountRepository;
import hu.finex.main.repository.SavingsAccountRepository;
import hu.finex.main.repository.SavingsTransactionRepository;
import hu.finex.main.security.CurrentUser;
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

    private static final String PARTNER_NAME = "FineX megtakarítás";

    @Mock private SavingsAccountRepository savingsAccountRepository;
    @Mock private SavingsTransactionRepository savingsTransactionRepository;
    @Mock private AccountRepository accountRepository;
    @Mock private SavingsAccountMapper savingsAccountMapper;
    @Mock private SavingsTransactionMapper savingsTransactionMapper;
    @Mock private LedgerService ledgerService;
    @Mock private NotificationService notificationService;
    @Mock private CurrentUser currentUser;
    @Spy private FinexProperties finexProperties = new FinexProperties();

    @InjectMocks private SavingsAccountService service;

    @Test
    void create_shouldCreateSavingsWithBankRate_andMoveInitialDeposit() {
        User user = User.builder().id(7L).build();
        when(currentUser.requireEntity()).thenReturn(user);
        when(savingsAccountRepository.existsByUser_IdAndNameIgnoreCaseAndStatusNot(7L, "Nyaralás", SavingsStatus.CLOSED)).thenReturn(false);

        Account current = account(1L, "HUF", "100000.00", AccountStatus.ACTIVE);
        lockAccount(7L, current);

        CreateSavingsAccountRequest req = CreateSavingsAccountRequest.builder()
                .name("Nyaralás")
                .accountId(1L)
                .initialDeposit(new BigDecimal("30000.00"))
                .targetAmount(new BigDecimal("600000.00"))
                .build();

        SavingsAccount mapped = savings(null, "HUF", "0", SavingsStatus.ACTIVE);
        when(savingsAccountMapper.toEntity(req, user, "HUF", new BigDecimal("3.50"))).thenReturn(mapped);

        SavingsAccount saved = savings(20L, "HUF", "0", SavingsStatus.ACTIVE);
        saved.setName("Nyaralás");
        when(savingsAccountRepository.save(mapped)).thenReturn(saved);

        SavingsTransaction deposit = SavingsTransaction.builder().type(SavingsTransactionType.DEPOSIT).build();
        when(savingsTransactionMapper.toEntity(saved, SavingsTransactionType.DEPOSIT, new BigDecimal("30000.00"))).thenReturn(deposit);

        SavingsAccountResponse expected = SavingsAccountResponse.builder().id(20L).build();
        when(savingsAccountMapper.toResponse(saved)).thenReturn(expected);

        SavingsAccountResponse resp = service.create(req);

        assertEquals(expected, resp);
        assertEquals(0, new BigDecimal("30000.00").compareTo(saved.getBalance()));
        verify(ledgerService).debit(current, TransactionType.OUTCOME, new BigDecimal("30000.00"), PARTNER_NAME, "Megtakarítás indítása: Nyaralás", null, null);
        verify(savingsTransactionRepository).save(deposit);
    }

    @Test
    void create_shouldNotMoveMoney_whenInitialDepositIsZero() {
        User user = User.builder().id(7L).build();
        when(currentUser.requireEntity()).thenReturn(user);
        when(savingsAccountRepository.existsByUser_IdAndNameIgnoreCaseAndStatusNot(7L, "Vésztartalék", SavingsStatus.CLOSED)).thenReturn(false);
        lockAccount(7L, account(1L, "HUF", "100.00", AccountStatus.ACTIVE));

        CreateSavingsAccountRequest req = CreateSavingsAccountRequest.builder()
                .name("Vésztartalék")
                .accountId(1L)
                .initialDeposit(BigDecimal.ZERO)
                .build();

        SavingsAccount mapped = savings(null, "HUF", "0", SavingsStatus.ACTIVE);
        when(savingsAccountMapper.toEntity(eq(req), eq(user), eq("HUF"), any())).thenReturn(mapped);
        when(savingsAccountRepository.save(mapped)).thenReturn(mapped);
        when(savingsAccountMapper.toResponse(mapped)).thenReturn(SavingsAccountResponse.builder().build());

        service.create(req);

        verifyNoInteractions(ledgerService, savingsTransactionRepository);
    }

    @Test
    void create_shouldThrowBusinessException_whenNameAlreadyExists() {
        User user = User.builder().id(7L).build();
        when(currentUser.requireEntity()).thenReturn(user);
        when(savingsAccountRepository.existsByUser_IdAndNameIgnoreCaseAndStatusNot(7L, "Nyaralás", SavingsStatus.CLOSED)).thenReturn(true);

        CreateSavingsAccountRequest req = CreateSavingsAccountRequest.builder()
                .name("Nyaralás")
                .accountId(1L)
                .initialDeposit(BigDecimal.ZERO)
                .build();

        assertThrows(BusinessException.class, () -> service.create(req));

        verify(savingsAccountRepository, never()).save(any());
        verifyNoInteractions(accountRepository, ledgerService);
    }

    @Test
    void create_shouldThrowNotFound_whenCurrentAccountIsNotOwn() {
        User user = User.builder().id(7L).build();
        when(currentUser.requireEntity()).thenReturn(user);
        when(savingsAccountRepository.existsByUser_IdAndNameIgnoreCaseAndStatusNot(7L, "Nyaralás", SavingsStatus.CLOSED)).thenReturn(false);
        when(accountRepository.existsByIdAndUser_Id(9L, 7L)).thenReturn(false);

        CreateSavingsAccountRequest req = CreateSavingsAccountRequest.builder()
                .name("Nyaralás")
                .accountId(9L)
                .initialDeposit(BigDecimal.ZERO)
                .build();

        assertThrows(NotFoundException.class, () -> service.create(req));

        verify(accountRepository, never()).findByIdForUpdate(any());
        verify(savingsAccountRepository, never()).save(any());
    }

    @Test
    void listMine_shouldReturnNotClosedSavings() {
        when(currentUser.requireId()).thenReturn(7L);

        SavingsAccount s1 = savings(1L, "HUF", "100.00", SavingsStatus.ACTIVE);
        when(savingsAccountRepository.findByUser_IdAndStatusNotOrderByCreatedAtAsc(7L, SavingsStatus.CLOSED)).thenReturn(List.of(s1));
        when(savingsAccountMapper.toResponse(s1)).thenReturn(SavingsAccountResponse.builder().id(1L).build());

        List<SavingsAccountResponse> out = service.listMine();

        assertEquals(1, out.size());
        assertEquals(1L, out.get(0).getId());
    }

    @Test
    void getById_shouldThrowNotFound_whenNotOwn() {
        when(currentUser.requireId()).thenReturn(7L);
        when(savingsAccountRepository.findByIdAndUser_Id(1L, 7L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.getById(1L));
    }

    @Test
    void update_shouldRenameAndChangeTarget() {
        when(currentUser.requireId()).thenReturn(7L);

        SavingsAccount entity = savings(1L, "HUF", "100.00", SavingsStatus.ACTIVE);
        entity.setName("Nyaralás");
        entity.setUser(User.builder().id(7L).build());
        when(savingsAccountRepository.findByIdAndUser_Id(1L, 7L)).thenReturn(Optional.of(entity));
        when(savingsAccountRepository.existsByUser_IdAndNameIgnoreCaseAndStatusNot(7L, "Nyaralás 2026", SavingsStatus.CLOSED)).thenReturn(false);

        UpdateSavingsAccountRequest req = UpdateSavingsAccountRequest.builder()
                .name("Nyaralás 2026")
                .targetAmount(new BigDecimal("800000.00"))
                .build();
        when(savingsAccountMapper.toResponse(entity)).thenReturn(SavingsAccountResponse.builder().id(1L).name("Nyaralás 2026").build());

        SavingsAccountResponse resp = service.update(1L, req);

        assertEquals("Nyaralás 2026", resp.getName());
        verify(savingsAccountMapper).updateEntity(entity, req);
    }

    @Test
    void update_shouldAllowChangingOnlyTheLetterCase_ofOwnName() {
        when(currentUser.requireId()).thenReturn(7L);

        SavingsAccount entity = savings(1L, "HUF", "100.00", SavingsStatus.ACTIVE);
        entity.setName("nyaralás");
        when(savingsAccountRepository.findByIdAndUser_Id(1L, 7L)).thenReturn(Optional.of(entity));
        when(savingsAccountMapper.toResponse(entity)).thenReturn(SavingsAccountResponse.builder().id(1L).build());

        UpdateSavingsAccountRequest req = UpdateSavingsAccountRequest.builder().name("Nyaralás").build();

        service.update(1L, req);

        verify(savingsAccountRepository, never()).existsByUser_IdAndNameIgnoreCaseAndStatusNot(any(), any(), any());
        verify(savingsAccountMapper).updateEntity(entity, req);
    }

    @Test
    void update_shouldThrowBusinessException_whenNameIsTaken() {
        when(currentUser.requireId()).thenReturn(7L);

        SavingsAccount entity = savings(1L, "HUF", "100.00", SavingsStatus.ACTIVE);
        entity.setName("Nyaralás");
        entity.setUser(User.builder().id(7L).build());
        when(savingsAccountRepository.findByIdAndUser_Id(1L, 7L)).thenReturn(Optional.of(entity));
        when(savingsAccountRepository.existsByUser_IdAndNameIgnoreCaseAndStatusNot(7L, "Vésztartalék", SavingsStatus.CLOSED)).thenReturn(true);

        UpdateSavingsAccountRequest req = UpdateSavingsAccountRequest.builder().name("Vésztartalék").build();

        assertThrows(BusinessException.class, () -> service.update(1L, req));

        verify(savingsAccountMapper, never()).updateEntity(any(), any());
    }

    @Test
    void update_shouldThrowBusinessException_whenClosed() {
        when(currentUser.requireId()).thenReturn(7L);
        when(savingsAccountRepository.findByIdAndUser_Id(1L, 7L)).thenReturn(Optional.of(savings(1L, "HUF", "0", SavingsStatus.CLOSED)));

        UpdateSavingsAccountRequest req = UpdateSavingsAccountRequest.builder().name("Új név").build();

        assertThrows(BusinessException.class, () -> service.update(1L, req));
    }

    @Test
    void depositFromAccount_shouldDebitAccount_andIncreaseSavings() {
        when(currentUser.requireId()).thenReturn(7L);

        Account current = account(1L, "HUF", "100000.00", AccountStatus.ACTIVE);
        SavingsAccount savings = savings(2L, "HUF", "50000.00", SavingsStatus.ACTIVE);
        lockAccount(7L, current);
        lockSavings(7L, savings);

        Transaction tx = Transaction.builder().id(55L).message("Havi félretétel").createdAt(Instant.parse("2025-03-10T10:00:00Z")).build();
        when(ledgerService.debit(current, TransactionType.OUTCOME, new BigDecimal("20000.00"), PARTNER_NAME, "Havi félretétel", null, null)).thenReturn(tx);

        SavingsTransferRequest req = SavingsTransferRequest.builder()
                .accountId(1L)
                .amount(new BigDecimal("20000.00"))
                .message("Havi félretétel")
                .build();

        SavingsTransferResponse resp = service.depositFromAccount(2L, req);

        assertEquals(0, new BigDecimal("70000.00").compareTo(savings.getBalance()));
        assertEquals(2L, resp.getSavingsAccountId());
        assertEquals(1L, resp.getAccountId());
        assertEquals(0, new BigDecimal("70000.00").compareTo(resp.getSavingsNewBalance()));
        assertEquals("Havi félretétel", resp.getMessage());
        verify(savingsTransactionMapper).toEntity(savings, SavingsTransactionType.DEPOSIT, new BigDecimal("20000.00"));
    }

    @Test
    void depositFromAccount_shouldLockCurrentAccountBeforeSavings() {
        when(currentUser.requireId()).thenReturn(7L);

        Account current = account(1L, "HUF", "100000.00", AccountStatus.ACTIVE);
        SavingsAccount savings = savings(2L, "HUF", "0", SavingsStatus.ACTIVE);
        lockAccount(7L, current);
        lockSavings(7L, savings);
        when(ledgerService.debit(any(), any(), any(), any(), any(), any(), any())).thenReturn(Transaction.builder().build());

        service.depositFromAccount(2L, SavingsTransferRequest.builder().accountId(1L).amount(BigDecimal.TEN).build());

        InOrder inOrder = inOrder(accountRepository, savingsAccountRepository);
        inOrder.verify(accountRepository).findByIdForUpdate(1L);
        inOrder.verify(savingsAccountRepository).findByIdForUpdate(2L);
    }

    @Test
    void depositFromAccount_shouldThrowBusinessException_whenCurrenciesDiffer() {
        when(currentUser.requireId()).thenReturn(7L);
        lockAccount(7L, account(1L, "EUR", "100.00", AccountStatus.ACTIVE));
        lockSavings(7L, savings(2L, "HUF", "0", SavingsStatus.ACTIVE));

        SavingsTransferRequest req = SavingsTransferRequest.builder().accountId(1L).amount(BigDecimal.TEN).build();

        assertThrows(BusinessException.class, () -> service.depositFromAccount(2L, req));

        verifyNoInteractions(ledgerService, savingsTransactionRepository);
    }

    @Test
    void depositFromAccount_shouldThrowBusinessException_whenSavingsIsFrozen() {
        when(currentUser.requireId()).thenReturn(7L);
        lockAccount(7L, account(1L, "HUF", "100.00", AccountStatus.ACTIVE));
        lockSavings(7L, savings(2L, "HUF", "0", SavingsStatus.FROZEN));

        SavingsTransferRequest req = SavingsTransferRequest.builder().accountId(1L).amount(BigDecimal.TEN).build();

        assertThrows(BusinessException.class, () -> service.depositFromAccount(2L, req));

        verifyNoInteractions(ledgerService);
    }

    @Test
    void withdrawToAccount_shouldDecreaseSavings_andCreditAccount() {
        when(currentUser.requireId()).thenReturn(7L);

        Account current = account(1L, "HUF", "0.00", AccountStatus.ACTIVE);
        SavingsAccount savings = savings(2L, "HUF", "50000.00", SavingsStatus.ACTIVE);
        lockAccount(7L, current);
        lockSavings(7L, savings);

        Transaction tx = Transaction.builder().id(56L).message("Megtakarítás kivétele innen: Teszt").build();
        when(ledgerService.credit(current, TransactionType.INCOME, new BigDecimal("15000.00"), PARTNER_NAME, "Megtakarítás kivétele innen: Teszt", null)).thenReturn(tx);

        SavingsTransferRequest req = SavingsTransferRequest.builder()
                .accountId(1L)
                .amount(new BigDecimal("15000.00"))
                .build();

        SavingsTransferResponse resp = service.withdrawToAccount(2L, req);

        assertEquals(0, new BigDecimal("35000.00").compareTo(savings.getBalance()));
        assertEquals(0, new BigDecimal("35000.00").compareTo(resp.getSavingsNewBalance()));
        verify(savingsTransactionMapper).toEntity(savings, SavingsTransactionType.WITHDRAWAL, new BigDecimal("15000.00"));
    }

    @Test
    void withdrawToAccount_shouldThrowBusinessException_whenSavingsBalanceIsTooLow() {
        when(currentUser.requireId()).thenReturn(7L);

        SavingsAccount savings = savings(2L, "HUF", "100.00", SavingsStatus.ACTIVE);
        lockAccount(7L, account(1L, "HUF", "0.00", AccountStatus.ACTIVE));
        lockSavings(7L, savings);

        SavingsTransferRequest req = SavingsTransferRequest.builder().accountId(1L).amount(new BigDecimal("100.01")).build();

        assertThrows(BusinessException.class, () -> service.withdrawToAccount(2L, req));

        assertEquals(0, new BigDecimal("100.00").compareTo(savings.getBalance()));
        verifyNoInteractions(ledgerService, savingsTransactionRepository);
    }

    @Test
    void close_shouldPayOutWholeBalance_andSetClosed() {
        when(currentUser.requireId()).thenReturn(7L);

        Account current = account(1L, "HUF", "0.00", AccountStatus.ACTIVE);
        SavingsAccount savings = savings(2L, "HUF", "42000.00", SavingsStatus.ACTIVE);
        lockAccount(7L, current);
        lockSavings(7L, savings);

        service.close(2L, 1L);

        assertEquals(SavingsStatus.CLOSED, savings.getStatus());
        assertEquals(0, BigDecimal.ZERO.compareTo(savings.getBalance()));
        verify(ledgerService).credit(current, TransactionType.INCOME, new BigDecimal("42000.00"), PARTNER_NAME, "Megtakarítás lezárása: Teszt", null);
    }

    @Test
    void close_shouldNotMoveMoney_whenSavingsIsEmpty() {
        when(currentUser.requireId()).thenReturn(7L);

        SavingsAccount savings = savings(2L, "HUF", "0.00", SavingsStatus.ACTIVE);
        lockAccount(7L, account(1L, "HUF", "0.00", AccountStatus.ACTIVE));
        lockSavings(7L, savings);

        service.close(2L, 1L);

        assertEquals(SavingsStatus.CLOSED, savings.getStatus());
        verifyNoInteractions(ledgerService, savingsTransactionRepository);
    }

    @Test
    void listTransactions_shouldThrowNotFound_whenSavingsIsNotOwn() {
        when(currentUser.requireId()).thenReturn(7L);
        when(savingsAccountRepository.existsByIdAndUser_Id(2L, 7L)).thenReturn(false);

        assertThrows(NotFoundException.class, () -> service.listTransactions(2L, PageRequest.of(0, 20)));

        verifyNoInteractions(savingsTransactionRepository);
    }

    @Test
    void listTransactions_shouldMapPage() {
        when(currentUser.requireId()).thenReturn(7L);
        when(savingsAccountRepository.existsByIdAndUser_Id(2L, 7L)).thenReturn(true);

        Pageable pageable = PageRequest.of(0, 20);
        SavingsTransaction st = SavingsTransaction.builder().id(9L).build();
        when(savingsTransactionRepository.findBySavingsAccount_IdOrderByCreatedAtDesc(2L, pageable)).thenReturn(new PageImpl<>(List.of(st), pageable, 1));
        when(savingsTransactionMapper.toResponse(st)).thenReturn(SavingsTransactionResponse.builder().id(9L).build());

        Page<SavingsTransactionResponse> page = service.listTransactions(2L, pageable);

        assertEquals(1, page.getTotalElements());
        assertEquals(9L, page.getContent().get(0).getId());
    }

    @Test
    void creditMonthlyInterest_shouldCreditOneTwelfthOfYearlyRate_andNotify() {
        User owner = User.builder().id(7L).build();
        SavingsAccount savings = savings(2L, "HUF", "120000.00", SavingsStatus.ACTIVE);
        savings.setUser(owner);
        savings.setInterestRate(new BigDecimal("3.50"));
        when(savingsAccountRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(savings));
        when(savingsTransactionRepository.existsBySavingsAccount_IdAndTypeAndCreatedAtGreaterThanEqual(eq(2L), eq(SavingsTransactionType.INTEREST), any())).thenReturn(false);

        boolean credited = service.creditMonthlyInterest(2L);

        // 120 000 * 3,5% / 12 = 350,00
        assertTrue(credited);
        assertEquals(0, new BigDecimal("120350.00").compareTo(savings.getBalance()));
        verify(savingsTransactionMapper).toEntity(savings, SavingsTransactionType.INTEREST, new BigDecimal("350.00"));
        verify(notificationService).notify(eq(owner), eq(NotificationType.SAVINGS), eq("Kamatjóváírás"), anyString());
    }

    @Test
    void creditMonthlyInterest_shouldSkip_whenAlreadyCreditedThisMonth() {
        SavingsAccount savings = savings(2L, "HUF", "120000.00", SavingsStatus.ACTIVE);
        when(savingsAccountRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(savings));
        when(savingsTransactionRepository.existsBySavingsAccount_IdAndTypeAndCreatedAtGreaterThanEqual(eq(2L), eq(SavingsTransactionType.INTEREST), any())).thenReturn(true);

        assertFalse(service.creditMonthlyInterest(2L));

        assertEquals(0, new BigDecimal("120000.00").compareTo(savings.getBalance()));
        verify(savingsTransactionRepository, never()).save(any());
        verifyNoInteractions(notificationService);
    }

    @Test
    void creditMonthlyInterest_shouldSkip_whenSavingsIsNotActive() {
        when(savingsAccountRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(savings(2L, "HUF", "1000.00", SavingsStatus.FROZEN)));

        assertFalse(service.creditMonthlyInterest(2L));

        verifyNoInteractions(savingsTransactionRepository, notificationService);
    }

    @Test
    void creditMonthlyInterest_shouldSkip_whenInterestRoundsToZero() {
        SavingsAccount savings = savings(2L, "HUF", "1.00", SavingsStatus.ACTIVE);
        savings.setInterestRate(new BigDecimal("3.50"));
        when(savingsAccountRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(savings));
        when(savingsTransactionRepository.existsBySavingsAccount_IdAndTypeAndCreatedAtGreaterThanEqual(eq(2L), eq(SavingsTransactionType.INTEREST), any())).thenReturn(false);

        assertFalse(service.creditMonthlyInterest(2L));

        verify(savingsTransactionRepository, never()).save(any());
    }

    private void lockAccount(Long userId, Account account) {
        when(accountRepository.existsByIdAndUser_Id(account.getId(), userId)).thenReturn(true);
        when(accountRepository.findByIdForUpdate(account.getId())).thenReturn(Optional.of(account));
    }

    private void lockSavings(Long userId, SavingsAccount savings) {
        when(savingsAccountRepository.existsByIdAndUser_Id(savings.getId(), userId)).thenReturn(true);
        when(savingsAccountRepository.findByIdForUpdate(savings.getId())).thenReturn(Optional.of(savings));
    }

    private Account account(Long id, String currency, String balance, AccountStatus status) {
        return Account.builder()
                .id(id)
                .currency(currency)
                .balance(new BigDecimal(balance))
                .status(status)
                .build();
    }

    private SavingsAccount savings(Long id, String currency, String balance, SavingsStatus status) {
        return SavingsAccount.builder()
                .id(id)
                .name("Teszt")
                .currency(currency)
                .balance(new BigDecimal(balance))
                .interestRate(new BigDecimal("3.50"))
                .status(status)
                .build();
    }
}
