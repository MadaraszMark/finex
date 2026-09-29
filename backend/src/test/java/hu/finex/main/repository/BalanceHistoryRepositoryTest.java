package hu.finex.main.repository;

import hu.finex.main.model.Account;
import hu.finex.main.model.BalanceHistory;
import hu.finex.main.model.User;
import hu.finex.main.model.enums.AccountStatus;
import hu.finex.main.model.enums.AccountType;
import hu.finex.main.model.enums.UserRole;
import hu.finex.main.model.enums.UserStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class BalanceHistoryRepositoryTest extends PostgresRepositoryTestBase {

    @Autowired private BalanceHistoryRepository balanceHistoryRepository;
    @Autowired private AccountRepository accountRepository;
    @Autowired private UserRepository userRepository;

    @Test
    void findByAccount_IdOrderByCreatedAtAsc_shouldReturnAscOrderedPage() {
        User user = saveUser("bh1@a.com");
        Account account = saveAccount(user, "BH-ACC-1");

        Instant t1 = Instant.parse("2025-01-01T10:00:00Z");
        Instant t2 = Instant.parse("2025-01-01T10:05:00Z");
        Instant t3 = Instant.parse("2025-01-01T10:10:00Z");

        saveHistory(account, new BigDecimal("100.00"), t2);
        saveHistory(account, new BigDecimal("90.00"), t1);
        saveHistory(account, new BigDecimal("110.00"), t3);

        Page<BalanceHistory> page = balanceHistoryRepository.findByAccount_IdOrderByCreatedAtAsc(
                account.getId(),
                PageRequest.of(0, 10)
        );

        assertEquals(3, page.getTotalElements());
        assertEquals(t1, page.getContent().get(0).getCreatedAt());
        assertEquals(t2, page.getContent().get(1).getCreatedAt());
        assertEquals(t3, page.getContent().get(2).getCreatedAt());
    }

    @Test
    void findByAccount_IdAndCreatedAtBetweenOrderByCreatedAtAsc_shouldFilterBetweenAndOrderAsc() {
        User user = saveUser("bh2@a.com");
        Account account = saveAccount(user, "BH-ACC-2");

        Instant t0 = Instant.parse("2025-01-01T09:59:00Z");
        Instant t1 = Instant.parse("2025-01-01T10:00:00Z");
        Instant t2 = Instant.parse("2025-01-01T10:05:00Z");
        Instant t3 = Instant.parse("2025-01-01T10:10:00Z");
        Instant t4 = Instant.parse("2025-01-01T10:11:00Z");

        saveHistory(account, new BigDecimal("1.00"), t0);
        saveHistory(account, new BigDecimal("2.00"), t1);
        saveHistory(account, new BigDecimal("3.00"), t2);
        saveHistory(account, new BigDecimal("4.00"), t3);
        saveHistory(account, new BigDecimal("5.00"), t4);

        Page<BalanceHistory> page = balanceHistoryRepository.findByAccount_IdAndCreatedAtBetweenOrderByCreatedAtAsc(
                account.getId(),
                t1,
                t3,
                PageRequest.of(0, 10)
        );

        assertEquals(3, page.getTotalElements());
        assertEquals(t1, page.getContent().get(0).getCreatedAt());
        assertEquals(t2, page.getContent().get(1).getCreatedAt());
        assertEquals(t3, page.getContent().get(2).getCreatedAt());

        List<BalanceHistory> list = balanceHistoryRepository.findByAccount_IdAndCreatedAtBetweenOrderByCreatedAtAsc(account.getId(), t1, t3);
        assertEquals(3, list.size());
    }

    @Test
    void existsByAccount_IdAndCreatedAtAfter_shouldWork() {
        User user = saveUser("bh3@a.com");
        Account account = saveAccount(user, "BH-ACC-3");

        Instant t1 = Instant.parse("2025-01-01T10:00:00Z");
        Instant t2 = Instant.parse("2025-01-01T11:00:00Z");

        saveHistory(account, new BigDecimal("10.00"), t1);
        saveHistory(account, new BigDecimal("20.00"), t2);

        assertTrue(balanceHistoryRepository.existsByAccount_IdAndCreatedAtAfter(account.getId(), Instant.parse("2025-01-01T10:30:00Z")));
        assertFalse(balanceHistoryRepository.existsByAccount_IdAndCreatedAtAfter(account.getId(), Instant.parse("2025-01-01T12:00:00Z")));
        assertFalse(balanceHistoryRepository.existsByAccount_IdAndCreatedAtAfter(account.getId(), t2));
    }

    private User saveUser(String email) {
        User user = User.builder()
                .firstName("Test")
                .lastName("User")
                .email(email)
                .phone("000")
                .passwordHash("HASH")
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        return userRepository.saveAndFlush(user);
    }

    private Account saveAccount(User user, String accountNumber) {
        Account account = Account.builder()
                .user(user)
                .name("Teszt számla")
                .accountNumber(accountNumber)
                .balance(BigDecimal.ZERO)
                .currency("HUF")
                .accountType(AccountType.CURRENT)
                .status(AccountStatus.ACTIVE)
                .createdAt(Instant.now())
                .build();
        return accountRepository.saveAndFlush(account);
    }

    private BalanceHistory saveHistory(Account account, BigDecimal balance, Instant createdAt) {
        BalanceHistory history = BalanceHistory.builder()
                .account(account)
                .balance(balance)
                .createdAt(createdAt)
                .build();
        return balanceHistoryRepository.saveAndFlush(history);
    }
}
