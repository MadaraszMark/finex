package hu.finex.main.service;

import hu.finex.main.dto.CategorySpendingResponse;
import hu.finex.main.dto.DashboardSummaryResponse;
import hu.finex.main.dto.MonthlySummaryResponse;
import hu.finex.main.exception.BusinessException;
import hu.finex.main.exception.NotFoundException;
import hu.finex.main.model.Account;
import hu.finex.main.model.enums.AccountStatus;
import hu.finex.main.model.enums.CardStatus;
import hu.finex.main.model.enums.SavingsStatus;
import hu.finex.main.model.enums.TransactionType;
import hu.finex.main.repository.AccountRepository;
import hu.finex.main.repository.CardRepository;
import hu.finex.main.repository.NotificationRepository;
import hu.finex.main.repository.ReportRepository;
import hu.finex.main.repository.SavingsAccountRepository;
import hu.finex.main.repository.TransactionCategoryRepository;
import hu.finex.main.repository.TransactionRepository;
import hu.finex.main.security.CurrentUser;
import hu.finex.main.util.DateUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StatisticsServiceTest {

    private static final List<TransactionType> INCOME_TYPES = List.of(TransactionType.INCOME, TransactionType.TRANSFER_IN);
    private static final List<TransactionType> OUTCOME_TYPES = List.of(TransactionType.OUTCOME, TransactionType.TRANSFER_OUT);

    @Mock private AccountRepository accountRepository;
    @Mock private SavingsAccountRepository savingsAccountRepository;
    @Mock private TransactionRepository transactionRepository;
    @Mock private TransactionCategoryRepository transactionCategoryRepository;
    @Mock private CardRepository cardRepository;
    @Mock private NotificationRepository notificationRepository;
    @Mock private ReportRepository reportRepository;
    @Mock private CurrentUser currentUser;

    @InjectMocks private StatisticsService service;

    @Test
    void getSummary_shouldCollectDashboardNumbers() {
        when(currentUser.requireId()).thenReturn(7L);
        when(accountRepository.sumBalanceByUser(7L, "HUF", AccountStatus.CLOSED)).thenReturn(new BigDecimal("1906620.00"));
        when(savingsAccountRepository.sumBalanceByUser(7L, "HUF", SavingsStatus.CLOSED)).thenReturn(new BigDecimal("450000.00"));
        when(transactionRepository.sumByUserAndTypesSince(eq(7L), eq("HUF"), eq(INCOME_TYPES), any())).thenReturn(new BigDecimal("685000.00"));
        when(transactionRepository.sumByUserAndTypesSince(eq(7L), eq("HUF"), eq(OUTCOME_TYPES), any())).thenReturn(new BigDecimal("312000.00"));
        when(accountRepository.countByUser_IdAndStatusNot(7L, AccountStatus.CLOSED)).thenReturn(2L);
        when(cardRepository.countByAccount_User_IdAndStatus(7L, CardStatus.ACTIVE)).thenReturn(2L);
        when(notificationRepository.countByUser_IdAndReadFalse(7L)).thenReturn(3L);

        DashboardSummaryResponse resp = service.getSummary("HUF");

        assertEquals("HUF", resp.getCurrency());
        assertEquals(new BigDecimal("1906620.00"), resp.getTotalBalance());
        assertEquals(new BigDecimal("450000.00"), resp.getSavingsBalance());
        assertEquals(new BigDecimal("685000.00"), resp.getMonthIncome());
        assertEquals(new BigDecimal("312000.00"), resp.getMonthOutcome());
        assertEquals(2L, resp.getAccountCount());
        assertEquals(2L, resp.getActiveCardCount());
        assertEquals(3L, resp.getUnreadNotificationCount());
    }

    @Test
    void getMonthlySummary_shouldFillMissingMonthsWithZero() {
        when(currentUser.requireId()).thenReturn(7L);

        Account huf = Account.builder().id(1L).currency("HUF").build();
        Account eur = Account.builder().id(2L).currency("EUR").build();
        when(accountRepository.findByUser_IdOrderByCreatedAtAsc(7L)).thenReturn(List.of(huf, eur));

        YearMonth thisMonth = YearMonth.now(DateUtils.BANK_ZONE);
        YearMonth firstMonth = thisMonth.minusMonths(2);

        MonthlySummaryResponse lastMonth = MonthlySummaryResponse.builder()
                .month(thisMonth.minusMonths(1).toString())
                .income(new BigDecimal("685000.00"))
                .outcome(new BigDecimal("300000.00"))
                .transactionCount(12)
                .build();
        // Csak a forintszámla vesz részt (devizanem szerinti szűrés)
        when(reportRepository.findMonthlySummary(List.of(1L), firstMonth.atDay(1))).thenReturn(List.of(lastMonth));

        List<MonthlySummaryResponse> result = service.getMonthlySummary(3, "HUF", null);

        assertEquals(3, result.size());
        assertEquals(firstMonth.toString(), result.get(0).getMonth());
        assertEquals(BigDecimal.ZERO, result.get(0).getIncome());
        assertEquals(0, result.get(0).getTransactionCount());
        assertEquals(lastMonth, result.get(1));
        assertEquals(thisMonth.toString(), result.get(2).getMonth());
    }

    @Test
    void getMonthlySummary_shouldUseOnlyTheGivenOwnAccount() {
        when(currentUser.requireId()).thenReturn(7L);
        when(accountRepository.findByIdAndUser_Id(2L, 7L)).thenReturn(Optional.of(Account.builder().id(2L).currency("EUR").build()));
        when(reportRepository.findMonthlySummary(eq(List.of(2L)), any())).thenReturn(List.of());

        List<MonthlySummaryResponse> result = service.getMonthlySummary(6, "HUF", 2L);

        assertEquals(6, result.size());
        verify(accountRepository, never()).findByUser_IdOrderByCreatedAtAsc(any());
    }

    @Test
    void getMonthlySummary_shouldThrowNotFound_whenAccountIsNotOwn() {
        when(currentUser.requireId()).thenReturn(7L);
        when(accountRepository.findByIdAndUser_Id(2L, 7L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.getMonthlySummary(6, "HUF", 2L));

        verifyNoInteractions(reportRepository);
    }

    @Test
    void getMonthlySummary_shouldThrowBusinessException_whenMonthsOutOfRange() {
        assertThrows(BusinessException.class, () -> service.getMonthlySummary(0, "HUF", null));
        assertThrows(BusinessException.class, () -> service.getMonthlySummary(25, "HUF", null));

        verifyNoInteractions(currentUser, reportRepository);
    }

    @Test
    void getCategorySpending_shouldDefaultToCurrentMonth() {
        when(currentUser.requireId()).thenReturn(7L);

        LocalDate today = DateUtils.today();
        List<CategorySpendingResponse> expected = List.of(new CategorySpendingResponse(1L, "Élelmiszer", "shopping-cart", new BigDecimal("52000.00"), 9L));
        when(transactionCategoryRepository.sumSpendingByCategory(7L, "HUF", OUTCOME_TYPES,
                DateUtils.startOfDay(today.withDayOfMonth(1)), DateUtils.startOfDay(today.plusDays(1)))).thenReturn(expected);

        List<CategorySpendingResponse> result = service.getCategorySpending(null, null, "HUF");

        assertEquals(expected, result);
    }

    @Test
    void getCategorySpending_shouldThrowBusinessException_whenFromIsAfterTo() {
        assertThrows(BusinessException.class, () -> service.getCategorySpending(LocalDate.of(2025, 4, 1), LocalDate.of(2025, 3, 1), "HUF"));

        verifyNoInteractions(transactionCategoryRepository);
    }
}
