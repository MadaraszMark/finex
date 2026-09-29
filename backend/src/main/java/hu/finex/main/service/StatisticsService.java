package hu.finex.main.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StatisticsService {

    private static final List<TransactionType> INCOME_TYPES = List.of(TransactionType.INCOME, TransactionType.TRANSFER_IN);
    private static final List<TransactionType> OUTCOME_TYPES = List.of(TransactionType.OUTCOME, TransactionType.TRANSFER_OUT);

    private final AccountRepository accountRepository;
    private final SavingsAccountRepository savingsAccountRepository;
    private final TransactionRepository transactionRepository;
    private final TransactionCategoryRepository transactionCategoryRepository;
    private final CardRepository cardRepository;
    private final NotificationRepository notificationRepository;
    private final ReportRepository reportRepository;
    private final CurrentUser currentUser;

    // A főoldal összesítője egyetlen hívással, egy devizában
    @Transactional(readOnly = true)
    public DashboardSummaryResponse getSummary(String currency) {
        Long userId = currentUser.requireId();
        Instant monthStart = DateUtils.startOfCurrentMonth();

        return DashboardSummaryResponse.builder()
                .currency(currency)
                .totalBalance(accountRepository.sumBalanceByUser(userId, currency, AccountStatus.CLOSED))
                .savingsBalance(savingsAccountRepository.sumBalanceByUser(userId, currency, SavingsStatus.CLOSED))
                .monthIncome(transactionRepository.sumByUserAndTypesSince(userId, currency, INCOME_TYPES, monthStart))
                .monthOutcome(transactionRepository.sumByUserAndTypesSince(userId, currency, OUTCOME_TYPES, monthStart))
                .accountCount(accountRepository.countByUser_IdAndStatusNot(userId, AccountStatus.CLOSED))
                .activeCardCount(cardRepository.countByAccount_User_IdAndStatus(userId, CardStatus.ACTIVE))
                .unreadNotificationCount(notificationRepository.countByUser_IdAndReadFalse(userId))
                .build();
    }

    // Havi bevétel és kiadás az elmúlt N hónapra (az adatbázis v_account_monthly_summary nézetéből).
    // A tétel nélküli hónapok is szerepelnek nullával, hogy a grafikon folytonos legyen.
    @Transactional(readOnly = true)
    public List<MonthlySummaryResponse> getMonthlySummary(int months, String currency, Long accountId) {
        if (months < 1 || months > 24) {
            throw new BusinessException("A hónapok száma 1 és 24 között lehet.");
        }

        Long userId = currentUser.requireId();

        List<Long> accountIds;
        if (accountId != null) {
            Account account = accountRepository.findByIdAndUser_Id(accountId, userId).orElseThrow(() -> new NotFoundException("Számla nem található."));
            accountIds = List.of(account.getId());
        } else {
            accountIds = accountRepository.findByUser_IdOrderByCreatedAtAsc(userId).stream()
                    .filter(account -> account.getCurrency().equals(currency))
                    .map(Account::getId)
                    .toList();
        }

        YearMonth firstMonth = YearMonth.now(DateUtils.BANK_ZONE).minusMonths(months - 1L);
        Map<String, MonthlySummaryResponse> byMonth = reportRepository.findMonthlySummary(accountIds, firstMonth.atDay(1)).stream()
                .collect(Collectors.toMap(MonthlySummaryResponse::getMonth, Function.identity()));

        List<MonthlySummaryResponse> result = new ArrayList<>();
        for (int i = 0; i < months; i++) {
            String month = firstMonth.plusMonths(i).toString();
            result.add(byMonth.getOrDefault(month, MonthlySummaryResponse.builder()
                    .month(month)
                    .income(BigDecimal.ZERO)
                    .outcome(BigDecimal.ZERO)
                    .transactionCount(0)
                    .build()));
        }
        return result;
    }

    // Költések kategóriánként egy időszakban (alapértelmezés: az aktuális hónap eleje és a mai nap között)
    @Transactional(readOnly = true)
    public List<CategorySpendingResponse> getCategorySpending(LocalDate from, LocalDate to, String currency) {
        LocalDate end = to != null ? to : DateUtils.today();
        LocalDate start = from != null ? from : end.withDayOfMonth(1);

        if (start.isAfter(end)) {
            throw new BusinessException("A kezdő dátum nem lehet későbbi a záró dátumnál.");
        }

        return transactionCategoryRepository.sumSpendingByCategory(currentUser.requireId(), currency, OUTCOME_TYPES,
                DateUtils.startOfDay(start), DateUtils.startOfDay(end.plusDays(1)));
    }
}
