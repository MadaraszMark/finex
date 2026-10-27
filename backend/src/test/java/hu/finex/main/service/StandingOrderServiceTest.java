package hu.finex.main.service;

import hu.finex.main.config.FinexProperties;
import hu.finex.main.dto.*;
import hu.finex.main.exception.BusinessException;
import hu.finex.main.exception.NotFoundException;
import hu.finex.main.mapper.StandingOrderMapper;
import hu.finex.main.model.Account;
import hu.finex.main.model.StandingOrder;
import hu.finex.main.model.User;
import hu.finex.main.model.enums.AccountStatus;
import hu.finex.main.model.enums.NotificationType;
import hu.finex.main.model.enums.StandingOrderFrequency;
import hu.finex.main.model.enums.UserStatus;
import hu.finex.main.repository.AccountRepository;
import hu.finex.main.repository.StandingOrderRepository;
import hu.finex.main.repository.UserRepository;
import hu.finex.main.security.CurrentUser;
import hu.finex.main.util.DateUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StandingOrderServiceTest {

    private static final String ANNA_IBAN = "HU15117730161111101800000001";
    private static final String LANDLORD_IBAN = "HU66109180010000048900240017";

    @Mock private StandingOrderRepository standingOrderRepository;
    @Mock private AccountRepository accountRepository;
    @Mock private UserRepository userRepository;
    @Mock private StandingOrderMapper standingOrderMapper;
    @Mock private TransactionService transactionService;
    @Mock private NotificationService notificationService;
    @Mock private CurrentUser currentUser;
    @Spy private FinexProperties finexProperties = new FinexProperties();

    @InjectMocks private StandingOrderService service;

    @Test
    void create_shouldSaveOrderWithNormalizedTarget() {
        when(currentUser.requireId()).thenReturn(7L);

        Account account = account(AccountStatus.ACTIVE);
        when(accountRepository.findByIdAndUser_Id(1L, 7L)).thenReturn(Optional.of(account));

        CreateStandingOrderRequest req = CreateStandingOrderRequest.builder()
                .accountId(1L)
                .toAccountNumber("hu66 1091 8001 0000 0489 0024 0017")
                .partnerName("Tóth Gábor")
                .amount(new BigDecimal("220000.00"))
                .frequency(StandingOrderFrequency.MONTHLY)
                .firstExecutionDate(LocalDate.of(2030, 1, 6))
                .build();

        StandingOrder mapped = StandingOrder.builder().account(account).toAccountNumber(LANDLORD_IBAN).build();
        when(standingOrderMapper.toEntity(req, account, LANDLORD_IBAN)).thenReturn(mapped);

        StandingOrder saved = StandingOrder.builder().id(3L).account(account).toAccountNumber(LANDLORD_IBAN).build();
        when(standingOrderRepository.save(mapped)).thenReturn(saved);
        when(standingOrderMapper.toResponse(saved)).thenReturn(StandingOrderResponse.builder().id(3L).toAccountNumber(LANDLORD_IBAN).build());

        StandingOrderResponse resp = service.create(req);

        assertEquals(3L, resp.getId());
        assertEquals(LANDLORD_IBAN, resp.getToAccountNumber());
    }

    @Test
    void create_shouldThrowBusinessException_whenTargetIsTheSourceAccount() {
        when(currentUser.requireId()).thenReturn(7L);
        when(accountRepository.findByIdAndUser_Id(1L, 7L)).thenReturn(Optional.of(account(AccountStatus.ACTIVE)));

        CreateStandingOrderRequest req = CreateStandingOrderRequest.builder()
                .accountId(1L)
                .toAccountNumber(ANNA_IBAN)
                .build();

        assertThrows(BusinessException.class, () -> service.create(req));

        verifyNoInteractions(standingOrderRepository);
    }

    @Test
    void create_shouldThrowBusinessException_whenSourceAccountIsNotActive() {
        when(currentUser.requireId()).thenReturn(7L);
        when(accountRepository.findByIdAndUser_Id(1L, 7L)).thenReturn(Optional.of(account(AccountStatus.FROZEN)));

        CreateStandingOrderRequest req = CreateStandingOrderRequest.builder()
                .accountId(1L)
                .toAccountNumber(LANDLORD_IBAN)
                .build();

        assertThrows(BusinessException.class, () -> service.create(req));

        verifyNoInteractions(standingOrderRepository);
    }

    @Test
    void create_shouldThrowNotFound_whenSourceAccountIsNotOwn() {
        when(currentUser.requireId()).thenReturn(7L);
        when(accountRepository.findByIdAndUser_Id(1L, 7L)).thenReturn(Optional.empty());

        CreateStandingOrderRequest req = CreateStandingOrderRequest.builder().accountId(1L).toAccountNumber(LANDLORD_IBAN).build();

        assertThrows(NotFoundException.class, () -> service.create(req));
    }

    @Test
    void update_shouldThrowBusinessException_whenOrderIsCancelled() {
        when(currentUser.requireId()).thenReturn(7L);

        StandingOrder order = StandingOrder.builder().id(3L).active(false).build();
        when(standingOrderRepository.findByIdAndAccount_User_Id(3L, 7L)).thenReturn(Optional.of(order));

        UpdateStandingOrderRequest req = UpdateStandingOrderRequest.builder().amount(BigDecimal.TEN).build();

        assertThrows(BusinessException.class, () -> service.update(3L, req));

        verify(standingOrderMapper, never()).updateEntity(any(), any());
    }

    @Test
    void cancel_shouldDeactivateOrder() {
        when(currentUser.requireId()).thenReturn(7L);

        StandingOrder order = StandingOrder.builder().id(3L).active(true).build();
        when(standingOrderRepository.findByIdAndAccount_User_Id(3L, 7L)).thenReturn(Optional.of(order));

        service.cancel(3L);

        assertFalse(order.isActive());
    }

    @Test
    void execute_shouldTransfer_andMoveToNextMonth() {
        User owner = User.builder().id(7L).status(UserStatus.ACTIVE).build();
        Account account = account(AccountStatus.ACTIVE);

        LocalDate today = DateUtils.today();
        StandingOrder order = StandingOrder.builder()
                .id(3L)
                .account(account)
                .toAccountNumber(LANDLORD_IBAN)
                .partnerName("Tóth Gábor")
                .amount(new BigDecimal("220000.00"))
                .message("Albérlet")
                .frequency(StandingOrderFrequency.MONTHLY)
                .nextExecutionDate(today)
                .executionDay(today.getDayOfMonth())
                .active(true)
                .build();

        when(standingOrderRepository.findByIdForUpdate(3L)).thenReturn(Optional.of(order));
        when(standingOrderRepository.findOwnerIdById(3L)).thenReturn(Optional.of(7L));
        when(userRepository.findById(7L)).thenReturn(Optional.of(owner));

        service.execute(3L);

        verify(transactionService).executeTransfer(owner, 1L, LANDLORD_IBAN, "Tóth Gábor", new BigDecimal("220000.00"), "Albérlet", null);
        assertEquals(today.plusMonths(1), order.getNextExecutionDate());
        assertNotNull(order.getLastExecutionAt());
        verify(notificationService).notify(eq(owner), eq(NotificationType.TRANSACTION), eq("Rendszeres átutalás teljesült"), anyString());
    }

    @Test
    void execute_shouldSkipMissedPeriods_forWeeklyOrder() {
        LocalDate today = DateUtils.today();
        StandingOrder order = StandingOrder.builder()
                .id(4L)
                .account(account(AccountStatus.ACTIVE))
                .toAccountNumber(LANDLORD_IBAN)
                .partnerName("Edzőterem")
                .amount(new BigDecimal("5000.00"))
                .frequency(StandingOrderFrequency.WEEKLY)
                .nextExecutionDate(today.minusDays(15))
                .active(true)
                .build();

        when(standingOrderRepository.findByIdForUpdate(4L)).thenReturn(Optional.of(order));
        when(standingOrderRepository.findOwnerIdById(4L)).thenReturn(Optional.of(7L));
        when(userRepository.findById(7L)).thenReturn(Optional.of(User.builder().id(7L).status(UserStatus.ACTIVE).build()));

        service.execute(4L);

        // Egyszer teljesül, a kimaradtakat nem pótolja: a következő a mai nap utáni első heti időpont
        assertEquals(today.minusDays(15).plusWeeks(3), order.getNextExecutionDate());
        verify(transactionService, times(1)).executeTransfer(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void execute_shouldDoNothing_whenOrderIsInactive() {
        StandingOrder order = StandingOrder.builder().id(3L).active(false).build();
        when(standingOrderRepository.findByIdForUpdate(3L)).thenReturn(Optional.of(order));

        service.execute(3L);

        verifyNoInteractions(transactionService, notificationService);
    }

    @Test
    void execute_shouldPropagateFailure_withoutMovingTheDate() {
        LocalDate today = DateUtils.today();
        User owner = User.builder().id(7L).status(UserStatus.ACTIVE).build();
        StandingOrder order = StandingOrder.builder()
                .id(3L)
                .account(account(AccountStatus.ACTIVE))
                .toAccountNumber(LANDLORD_IBAN)
                .partnerName("Tóth Gábor")
                .amount(new BigDecimal("220000.00"))
                .frequency(StandingOrderFrequency.MONTHLY)
                .nextExecutionDate(today)
                .executionDay(today.getDayOfMonth())
                .active(true)
                .build();

        when(standingOrderRepository.findByIdForUpdate(3L)).thenReturn(Optional.of(order));
        when(standingOrderRepository.findOwnerIdById(3L)).thenReturn(Optional.of(7L));
        when(userRepository.findById(7L)).thenReturn(Optional.of(owner));
        when(transactionService.executeTransfer(any(), any(), any(), any(), any(), any(), any())).thenThrow(new BusinessException("Nincs elegendő fedezet a számlán."));

        assertThrows(BusinessException.class, () -> service.execute(3L));

        assertEquals(today, order.getNextExecutionDate());
        verifyNoInteractions(notificationService);
    }

    @Test
    void handleFailedExecution_shouldMoveToNextDate_andNotifyOwner() {
        LocalDate today = DateUtils.today();
        User owner = User.builder().id(7L).status(UserStatus.ACTIVE).build();
        Account account = account(AccountStatus.ACTIVE);
        account.setUser(owner);

        StandingOrder order = StandingOrder.builder()
                .id(3L)
                .account(account)
                .partnerName("Tóth Gábor")
                .frequency(StandingOrderFrequency.MONTHLY)
                .nextExecutionDate(today)
                .executionDay(today.getDayOfMonth())
                .active(true)
                .build();
        when(standingOrderRepository.findByIdForUpdate(3L)).thenReturn(Optional.of(order));

        service.handleFailedExecution(3L, "Nincs elegendő fedezet a számlán.");

        assertEquals(today.plusMonths(1), order.getNextExecutionDate());
        assertTrue(order.isActive());
        verify(notificationService).notify(eq(owner), eq(NotificationType.TRANSACTION), eq("Sikertelen rendszeres átutalás"), contains("Nincs elegendő fedezet"));
    }

    @Test
    void findDueOrderIds_shouldQueryWithToday() {
        when(standingOrderRepository.findDueOrderIds(DateUtils.today())).thenReturn(List.of(3L, 4L));

        assertEquals(List.of(3L, 4L), service.findDueOrderIds());
    }

    // A 31-i teljesítés februárban a hó utolsó napjára kerül, márciusban viszont újra 31-re
    @Test
    void nextExecutionDate_shouldKeepTheExecutionDay_forMonthlyOrder() {
        StandingOrder order = StandingOrder.builder()
                .frequency(StandingOrderFrequency.MONTHLY)
                .executionDay(31)
                .nextExecutionDate(LocalDate.of(2027, 1, 31))
                .build();

        LocalDate february = StandingOrderService.nextExecutionDate(order, LocalDate.of(2027, 1, 31));
        assertEquals(LocalDate.of(2027, 2, 28), february);

        order.setNextExecutionDate(february);
        assertEquals(LocalDate.of(2027, 3, 31), StandingOrderService.nextExecutionDate(order, february));
    }

    // Ütemező és kézi admin indítás egyszerre: a második már a továbblépett dátumot látja
    @Test
    void execute_shouldDoNothing_whenOrderIsNoLongerDue() {
        LocalDate tomorrow = DateUtils.today().plusDays(1);
        StandingOrder order = StandingOrder.builder()
                .id(3L)
                .account(account(AccountStatus.ACTIVE))
                .frequency(StandingOrderFrequency.MONTHLY)
                .nextExecutionDate(tomorrow)
                .executionDay(tomorrow.getDayOfMonth())
                .active(true)
                .build();
        when(standingOrderRepository.findByIdForUpdate(3L)).thenReturn(Optional.of(order));

        service.execute(3L);

        verifyNoInteractions(transactionService, notificationService);
    }

    @Test
    void execute_shouldFail_whenOwnerIsBlocked() {
        LocalDate today = DateUtils.today();
        StandingOrder order = StandingOrder.builder()
                .id(3L)
                .account(account(AccountStatus.ACTIVE))
                .frequency(StandingOrderFrequency.MONTHLY)
                .nextExecutionDate(today)
                .executionDay(today.getDayOfMonth())
                .active(true)
                .build();
        when(standingOrderRepository.findByIdForUpdate(3L)).thenReturn(Optional.of(order));
        when(standingOrderRepository.findOwnerIdById(3L)).thenReturn(Optional.of(7L));
        when(userRepository.findById(7L)).thenReturn(Optional.of(User.builder().id(7L).status(UserStatus.BLOCKED).build()));

        assertThrows(BusinessException.class, () -> service.execute(3L));

        verifyNoInteractions(transactionService);
    }

    private Account account(AccountStatus status) {
        return Account.builder()
                .id(1L)
                .accountNumber(ANNA_IBAN)
                .user(User.builder().id(7L).status(UserStatus.ACTIVE).build())
                .currency("HUF")
                .status(status)
                .build();
    }
}
