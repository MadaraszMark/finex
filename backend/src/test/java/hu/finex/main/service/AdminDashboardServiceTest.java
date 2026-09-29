package hu.finex.main.service;

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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminDashboardServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private AccountRepository accountRepository;
    @Mock private TransactionRepository transactionRepository;
    @Mock private SupportTicketRepository supportTicketRepository;
    @Mock private LoginLogRepository loginLogRepository;

    @InjectMocks private AdminDashboardService service;

    @Test
    void getDashboard_shouldCollectBankWideNumbers() {
        when(userRepository.count()).thenReturn(3L);
        when(userRepository.countByStatus(UserStatus.BLOCKED)).thenReturn(0L);
        when(accountRepository.countByStatusNot(AccountStatus.CLOSED)).thenReturn(4L);
        when(accountRepository.sumBalanceByCurrency("HUF", AccountStatus.CLOSED)).thenReturn(new BigDecimal("5059120.00"));
        when(transactionRepository.countByCreatedAtGreaterThanEqual(any())).thenReturn(12L);
        when(transactionRepository.sumVolumeSince(eq("HUF"), eq(List.of(TransactionType.OUTCOME, TransactionType.TRANSFER_OUT)), any())).thenReturn(new BigDecimal("250000.00"));
        when(supportTicketRepository.countByStatusNot(TicketStatus.RESOLVED)).thenReturn(1L);
        when(loginLogRepository.countByStatusAndCreatedAtAfter(eq(LoginStatus.FAILED), any())).thenReturn(2L);

        AdminDashboardResponse resp = service.getDashboard();

        assertEquals(3L, resp.getUserCount());
        assertEquals(0L, resp.getBlockedUserCount());
        assertEquals(4L, resp.getAccountCount());
        assertEquals(new BigDecimal("5059120.00"), resp.getTotalDepositsHuf());
        assertEquals(12L, resp.getTransactionsToday());
        assertEquals(new BigDecimal("250000.00"), resp.getTransactionVolumeTodayHuf());
        assertEquals(1L, resp.getOpenTicketCount());
        assertEquals(2L, resp.getFailedLoginsLast24h());
    }
}
