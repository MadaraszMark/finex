package hu.finex.main.service;

import hu.finex.main.config.FinexProperties;
import hu.finex.main.dto.*;
import hu.finex.main.exception.BusinessException;
import hu.finex.main.exception.NotFoundException;
import hu.finex.main.mapper.AccountMapper;
import hu.finex.main.model.*;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock private AccountRepository accountRepository;
    @Mock private AccountMapper accountMapper;
    @Mock private CardService cardService;
    @Mock private LedgerService ledgerService;
    @Mock private NotificationService notificationService;
    @Mock private StandingOrderRepository standingOrderRepository;
    @Mock private ReportRepository reportRepository;
    @Mock private CurrentUser currentUser;
    @Spy private FinexProperties finexProperties = new FinexProperties();

    @InjectMocks private AccountService service;

    @Test
    void listMine_shouldReturnOwnAccountsInOrder() {
        when(currentUser.requireId()).thenReturn(7L);

        Account a1 = Account.builder().id(1L).build();
        Account a2 = Account.builder().id(2L).build();
        when(accountRepository.findByUser_IdOrderByCreatedAtAsc(7L)).thenReturn(List.of(a1, a2));
        when(accountMapper.toResponse(a1)).thenReturn(AccountResponse.builder().id(1L).build());
        when(accountMapper.toResponse(a2)).thenReturn(AccountResponse.builder().id(2L).build());

        List<AccountResponse> out = service.listMine();

        assertEquals(2, out.size());
        assertEquals(1L, out.get(0).getId());
        assertEquals(2L, out.get(1).getId());
        verify(accountRepository).findByUser_IdOrderByCreatedAtAsc(7L);
    }

    @Test
    void getById_shouldReturnResponse_whenOwned() {
        when(currentUser.requireId()).thenReturn(7L);

        Account acc = Account.builder().id(5L).build();
        when(accountRepository.findByIdAndUser_Id(5L, 7L)).thenReturn(Optional.of(acc));
        when(accountMapper.toResponse(acc)).thenReturn(AccountResponse.builder().id(5L).build());

        AccountResponse resp = service.getById(5L);

        assertEquals(5L, resp.getId());
        verify(accountRepository).findByIdAndUser_Id(5L, 7L);
        verify(accountMapper).toResponse(acc);
    }

    @Test
    void getById_shouldThrowNotFound_whenAccountBelongsToOtherUser() {
        when(currentUser.requireId()).thenReturn(7L);
        when(accountRepository.findByIdAndUser_Id(5L, 7L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.getById(5L));

        verifyNoInteractions(accountMapper);
    }

    @Test
    void getMyAccount_shouldReturnOldestActiveCurrentAccount() {
        when(currentUser.requireId()).thenReturn(7L);

        Account acc = Account.builder().id(1L).build();
        when(accountRepository.findFirstByUser_IdAndStatusAndAccountTypeOrderByCreatedAtAsc(7L, AccountStatus.ACTIVE, AccountType.CURRENT)).thenReturn(Optional.of(acc));
        when(accountMapper.toResponse(acc)).thenReturn(AccountResponse.builder().id(1L).build());

        AccountResponse resp = service.getMyAccount();

        assertEquals(1L, resp.getId());
    }

    @Test
    void getMyAccount_shouldThrowNotFound_whenNoActiveCurrent() {
        when(currentUser.requireId()).thenReturn(7L);
        when(accountRepository.findFirstByUser_IdAndStatusAndAccountTypeOrderByCreatedAtAsc(7L, AccountStatus.ACTIVE, AccountType.CURRENT)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.getMyAccount());
    }

    @Test
    void open_shouldCreateAccountWithValidIbanAndCard() {
        User user = User.builder().id(7L).build();
        when(currentUser.requireEntity()).thenReturn(user);
        when(accountRepository.countByUser_IdAndStatusNot(7L, AccountStatus.CLOSED)).thenReturn(1L);
        when(accountRepository.existsByAccountNumber(anyString())).thenReturn(false);

        CreateAccountRequest req = CreateAccountRequest.builder()
                .name("Euró számla")
                .currency("EUR")
                .build();

        Account mapped = Account.builder().user(user).name("Euró számla").currency("EUR").build();
        when(accountMapper.toEntity(eq(req), eq(user), anyString())).thenReturn(mapped);

        Account saved = Account.builder().id(10L).user(user).name("Euró számla").currency("EUR").status(AccountStatus.ACTIVE).build();
        when(accountRepository.save(mapped)).thenReturn(saved);
        when(accountMapper.toResponse(saved)).thenReturn(AccountResponse.builder().id(10L).name("Euró számla").build());

        AccountResponse resp = service.open(req);

        assertEquals(10L, resp.getId());
        assertEquals("Euró számla", resp.getName());

        // A generált számlaszám érvényes magyar IBAN
        ArgumentCaptor<String> numberCaptor = ArgumentCaptor.forClass(String.class);
        verify(accountMapper).toEntity(eq(req), eq(user), numberCaptor.capture());
        assertTrue(IbanUtils.isValid(numberCaptor.getValue()));
        assertTrue(numberCaptor.getValue().startsWith("HU"));

        verify(cardService).createCard(saved);
    }

    @Test
    void open_shouldThrowBusinessException_whenAccountLimitReached() {
        User user = User.builder().id(7L).build();
        when(currentUser.requireEntity()).thenReturn(user);
        when(accountRepository.countByUser_IdAndStatusNot(7L, AccountStatus.CLOSED)).thenReturn(5L);

        CreateAccountRequest req = CreateAccountRequest.builder().name("Hatodik").currency("HUF").build();

        assertThrows(BusinessException.class, () -> service.open(req));

        verify(accountRepository, never()).save(any());
        verifyNoInteractions(accountMapper, cardService);
    }

    @Test
    void createDefaultAccount_shouldCreateActiveHufCurrentAccountWithCard() {
        User user = User.builder().id(7L).build();
        when(accountRepository.existsByAccountNumber(anyString())).thenReturn(false);
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Account account = service.createDefaultAccount(user);

        assertEquals(user, account.getUser());
        assertEquals("Fő számla", account.getName());
        assertEquals("HUF", account.getCurrency());
        assertEquals(AccountType.CURRENT, account.getAccountType());
        assertEquals(AccountStatus.ACTIVE, account.getStatus());
        assertEquals(BigDecimal.ZERO, account.getBalance());
        assertTrue(IbanUtils.isValid(account.getAccountNumber()));
        verify(cardService).createCard(account);
    }

    @Test
    void deposit_shouldCreditThroughLedger() {
        when(currentUser.requireId()).thenReturn(7L);
        when(accountRepository.existsByIdAndUser_Id(10L, 7L)).thenReturn(true);

        Account acc = Account.builder()
                .id(10L)
                .balance(new BigDecimal("1000.00"))
                .currency("HUF")
                .status(AccountStatus.ACTIVE)
                .build();
        when(accountRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(acc));

        AccountResponse expected = AccountResponse.builder().id(10L).build();
        when(accountMapper.toResponse(acc)).thenReturn(expected);

        DepositRequest req = DepositRequest.builder()
                .amount(new BigDecimal("250.00"))
                .message("Topup")
                .build();

        AccountResponse resp = service.deposit(10L, req);

        assertEquals(expected, resp);
        verify(ledgerService).credit(acc, TransactionType.INCOME, new BigDecimal("250.00"), "Készpénzbefizetés", "Topup", null);
    }

    @Test
    void deposit_shouldThrowNotFound_whenNotOwner_withoutLockingTheAccount() {
        when(currentUser.requireId()).thenReturn(7L);
        when(accountRepository.existsByIdAndUser_Id(10L, 7L)).thenReturn(false);

        DepositRequest req = DepositRequest.builder().amount(BigDecimal.TEN).build();

        assertThrows(NotFoundException.class, () -> service.deposit(10L, req));

        verify(accountRepository, never()).findByIdForUpdate(any());
        verifyNoInteractions(ledgerService, accountMapper);
    }

    @Test
    void deposit_shouldThrowBusinessException_whenAccountFrozen() {
        when(currentUser.requireId()).thenReturn(7L);
        when(accountRepository.existsByIdAndUser_Id(10L, 7L)).thenReturn(true);

        Account acc = Account.builder().id(10L).balance(BigDecimal.ZERO).status(AccountStatus.FROZEN).build();
        when(accountRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(acc));

        DepositRequest req = DepositRequest.builder().amount(BigDecimal.TEN).build();

        assertThrows(BusinessException.class, () -> service.deposit(10L, req));

        verifyNoInteractions(ledgerService);
    }

    @Test
    void close_shouldCloseAccount_cancelCards_andStopStandingOrders() {
        User owner = User.builder().id(7L).build();
        when(currentUser.requireId()).thenReturn(7L);
        when(accountRepository.existsByIdAndUser_Id(3L, 7L)).thenReturn(true);

        Account acc = Account.builder().id(3L).user(owner).balance(new BigDecimal("0.00")).status(AccountStatus.ACTIVE).build();
        when(accountRepository.findByIdForUpdate(3L)).thenReturn(Optional.of(acc));
        when(accountRepository.countByUser_IdAndStatusNot(7L, AccountStatus.CLOSED)).thenReturn(2L);

        StandingOrder order = StandingOrder.builder().id(1L).active(true).build();
        when(standingOrderRepository.findByAccount_IdAndActiveTrue(3L)).thenReturn(List.of(order));

        service.close(3L);

        assertEquals(AccountStatus.CLOSED, acc.getStatus());
        assertFalse(order.isActive());
        verify(cardService).cancelCardsOfAccount(3L);
    }

    @Test
    void close_shouldThrowBusinessException_whenBalanceIsNotZero() {
        User owner = User.builder().id(7L).build();
        when(currentUser.requireId()).thenReturn(7L);
        when(accountRepository.existsByIdAndUser_Id(3L, 7L)).thenReturn(true);

        Account acc = Account.builder().id(3L).user(owner).balance(new BigDecimal("0.01")).status(AccountStatus.ACTIVE).build();
        when(accountRepository.findByIdForUpdate(3L)).thenReturn(Optional.of(acc));

        assertThrows(BusinessException.class, () -> service.close(3L));

        assertEquals(AccountStatus.ACTIVE, acc.getStatus());
        verifyNoInteractions(cardService, standingOrderRepository);
    }

    @Test
    void close_shouldThrowBusinessException_whenLastOpenAccount() {
        User owner = User.builder().id(7L).build();
        when(currentUser.requireId()).thenReturn(7L);
        when(accountRepository.existsByIdAndUser_Id(3L, 7L)).thenReturn(true);

        Account acc = Account.builder().id(3L).user(owner).balance(BigDecimal.ZERO).status(AccountStatus.ACTIVE).build();
        when(accountRepository.findByIdForUpdate(3L)).thenReturn(Optional.of(acc));
        when(accountRepository.countByUser_IdAndStatusNot(7L, AccountStatus.CLOSED)).thenReturn(1L);

        assertThrows(BusinessException.class, () -> service.close(3L));

        assertEquals(AccountStatus.ACTIVE, acc.getStatus());
        verifyNoInteractions(cardService);
    }

    @Test
    void close_shouldThrowBusinessException_whenAlreadyClosed() {
        when(currentUser.requireId()).thenReturn(7L);
        when(accountRepository.existsByIdAndUser_Id(3L, 7L)).thenReturn(true);

        Account acc = Account.builder().id(3L).balance(BigDecimal.ZERO).status(AccountStatus.CLOSED).build();
        when(accountRepository.findByIdForUpdate(3L)).thenReturn(Optional.of(acc));

        assertThrows(BusinessException.class, () -> service.close(3L));
    }

    @Test
    void getStatement_shouldUseDatabaseFunctions_andCalculateTotals() {
        when(currentUser.requireId()).thenReturn(7L);

        Account acc = Account.builder().id(3L).accountNumber("HU15117730161111101800000001").currency("HUF").build();
        when(accountRepository.findByIdAndUser_Id(3L, 7L)).thenReturn(Optional.of(acc));

        LocalDate from = LocalDate.of(2025, 3, 1);
        LocalDate to = LocalDate.of(2025, 3, 31);

        when(reportRepository.findBalanceAt(3L, DateUtils.startOfDay(from))).thenReturn(new BigDecimal("100000.00"));

        StatementItemResponse salary = StatementItemResponse.builder()
                .signedAmount(new BigDecimal("685000.00"))
                .runningBalance(new BigDecimal("785000.00"))
                .build();
        StatementItemResponse rent = StatementItemResponse.builder()
                .signedAmount(new BigDecimal("-220000.00"))
                .runningBalance(new BigDecimal("565000.00"))
                .build();
        when(reportRepository.findStatement(3L, DateUtils.startOfDay(from), DateUtils.startOfDay(to.plusDays(1)))).thenReturn(List.of(salary, rent));

        StatementResponse resp = service.getStatement(3L, from, to);

        assertEquals(3L, resp.getAccountId());
        assertEquals("HU15117730161111101800000001", resp.getAccountNumber());
        assertEquals("HUF", resp.getCurrency());
        assertEquals(from, resp.getFrom());
        assertEquals(to, resp.getTo());
        assertEquals(new BigDecimal("100000.00"), resp.getOpeningBalance());
        assertEquals(new BigDecimal("565000.00"), resp.getClosingBalance());
        assertEquals(new BigDecimal("685000.00"), resp.getTotalIncome());
        assertEquals(new BigDecimal("220000.00"), resp.getTotalOutcome());
        assertEquals(2, resp.getItems().size());
    }

    @Test
    void getStatement_shouldReturnOpeningAsClosing_whenNoItems() {
        when(currentUser.requireId()).thenReturn(7L);

        Account acc = Account.builder().id(3L).currency("HUF").build();
        when(accountRepository.findByIdAndUser_Id(3L, 7L)).thenReturn(Optional.of(acc));

        LocalDate day = LocalDate.of(2025, 3, 1);
        when(reportRepository.findBalanceAt(eq(3L), any())).thenReturn(new BigDecimal("500.00"));
        when(reportRepository.findStatement(eq(3L), any(), any())).thenReturn(List.of());

        StatementResponse resp = service.getStatement(3L, day, day);

        assertEquals(new BigDecimal("500.00"), resp.getClosingBalance());
        assertEquals(BigDecimal.ZERO, resp.getTotalIncome());
        assertEquals(BigDecimal.ZERO, resp.getTotalOutcome());
    }

    @Test
    void getStatement_shouldThrowBusinessException_whenFromIsAfterTo() {
        when(currentUser.requireId()).thenReturn(7L);
        when(accountRepository.findByIdAndUser_Id(3L, 7L)).thenReturn(Optional.of(Account.builder().id(3L).build()));

        assertThrows(BusinessException.class, () -> service.getStatement(3L, LocalDate.of(2025, 4, 1), LocalDate.of(2025, 3, 1)));

        verifyNoInteractions(reportRepository);
    }

    @Test
    void listAll_shouldFilterByStatus_whenGiven() {
        Pageable pageable = PageRequest.of(0, 20);
        Account frozen = Account.builder().id(1L).status(AccountStatus.FROZEN).build();
        when(accountRepository.findByStatus(AccountStatus.FROZEN, pageable)).thenReturn(new PageImpl<>(List.of(frozen), pageable, 1));
        when(accountMapper.toResponse(frozen)).thenReturn(AccountResponse.builder().id(1L).status(AccountStatus.FROZEN).build());

        Page<AccountResponse> page = service.listAll(AccountStatus.FROZEN, pageable);

        assertEquals(1, page.getTotalElements());
        verify(accountRepository, never()).findAll(any(Pageable.class));
    }

    @Test
    void updateStatus_shouldFreezeAccount_andNotifyOwner() {
        User owner = User.builder().id(7L).build();
        Account acc = Account.builder().id(2L).user(owner).name("Fő számla").status(AccountStatus.ACTIVE).build();
        when(accountRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(acc));

        UpdateAccountStatusRequest req = UpdateAccountStatusRequest.builder().status(AccountStatus.FROZEN).build();
        doAnswer(invocation -> {
            acc.setStatus(AccountStatus.FROZEN);
            return null;
        }).when(accountMapper).updateStatus(acc, req);

        AccountResponse expected = AccountResponse.builder().id(2L).status(AccountStatus.FROZEN).build();
        when(accountMapper.toResponse(acc)).thenReturn(expected);

        AccountResponse resp = service.updateStatus(2L, req);

        assertEquals(AccountStatus.FROZEN, resp.getStatus());
        verify(notificationService).notify(eq(owner), eq(NotificationType.SECURITY), anyString(), contains("befagyasztott"));
        verifyNoInteractions(cardService);
    }

    @Test
    void updateStatus_shouldCloseAccountWithCards_whenClosedAndBalanceZero() {
        User owner = User.builder().id(7L).build();
        Account acc = Account.builder().id(2L).user(owner).name("Fő számla").balance(BigDecimal.ZERO).status(AccountStatus.ACTIVE).build();
        when(accountRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(acc));
        when(standingOrderRepository.findByAccount_IdAndActiveTrue(2L)).thenReturn(List.of());

        UpdateAccountStatusRequest req = UpdateAccountStatusRequest.builder().status(AccountStatus.CLOSED).build();
        when(accountMapper.toResponse(acc)).thenReturn(AccountResponse.builder().id(2L).status(AccountStatus.CLOSED).build());

        service.updateStatus(2L, req);

        assertEquals(AccountStatus.CLOSED, acc.getStatus());
        verify(cardService).cancelCardsOfAccount(2L);
        verify(notificationService).notify(eq(owner), eq(NotificationType.SECURITY), anyString(), contains("lezárt"));
    }

    @Test
    void updateStatus_shouldThrowBusinessException_whenAccountAlreadyClosed() {
        Account acc = Account.builder().id(2L).status(AccountStatus.CLOSED).build();
        when(accountRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(acc));

        UpdateAccountStatusRequest req = UpdateAccountStatusRequest.builder().status(AccountStatus.ACTIVE).build();

        assertThrows(BusinessException.class, () -> service.updateStatus(2L, req));

        verifyNoInteractions(notificationService);
    }
}
