package hu.finex.main.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hu.finex.main.dto.AdminDashboardResponse;
import hu.finex.main.model.enums.AccountStatus;
import hu.finex.main.model.enums.LoginStatus;
import hu.finex.main.model.enums.TicketStatus;
import hu.finex.main.model.enums.TransactionType;
import hu.finex.main.model.enums.UserStatus;
import hu.finex.main.repository.AccountRepository;
import hu.finex.main.repository.LoginLogRepository;
import hu.finex.main.repository.SupportTicketRepository;
import hu.finex.main.repository.TransactionRepository;
import hu.finex.main.repository.UserRepository;
import hu.finex.main.util.DateUtils;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminDashboardService {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final SupportTicketRepository supportTicketRepository;
    private final LoginLogRepository loginLogRepository;

    // A bank egészére vonatkozó mutatók az admin főoldalhoz
    @Transactional(readOnly = true)
    public AdminDashboardResponse getDashboard() {
        Instant todayStart = DateUtils.startOfToday();

        return AdminDashboardResponse.builder()
                .userCount(userRepository.count())
                .blockedUserCount(userRepository.countByStatus(UserStatus.BLOCKED))
                .accountCount(accountRepository.countByStatusNot(AccountStatus.CLOSED))
                .totalDepositsHuf(accountRepository.sumBalanceByCurrency("HUF", AccountStatus.CLOSED))
                .transactionsToday(transactionRepository.countByCreatedAtGreaterThanEqual(todayStart))
                .transactionVolumeTodayHuf(transactionRepository.sumVolumeSince("HUF", List.of(TransactionType.OUTCOME, TransactionType.TRANSFER_OUT), todayStart))
                .openTicketCount(supportTicketRepository.countByStatusNot(TicketStatus.RESOLVED))
                .failedLoginsLast24h(loginLogRepository.countByStatusAndCreatedAtAfter(LoginStatus.FAILED, Instant.now().minus(24, ChronoUnit.HOURS)))
                .build();
    }
}
