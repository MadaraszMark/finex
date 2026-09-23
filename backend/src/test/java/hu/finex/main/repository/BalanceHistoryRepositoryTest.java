package hu.finex.main.repository;

import hu.finex.main.model.Account;
import hu.finex.main.model.BalanceHistory;
import hu.finex.main.model.User;
import hu.finex.main.model.enums.AccountStatus;
import hu.finex.main.model.enums.AccountType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class BalanceHistoryRepositoryTest extends PostgresRepositoryTestBase {

    @Autowired private BalanceHistoryRepository balanceHistoryRepository;
    @Autowired private AccountRepository accountRepository;
    @Autowired private UserRepository userRepository;

    @Test
    void findByAccount_IdOrderByCreatedAtAsc_shouldReturnAscOrderedPage() {
        User user = saveUser("bh1@a.com");
        Account account = saveAccount(user, "BH-ACC-1");

        OffsetDateTime t1 = OffsetDateTime.of(2025, 1, 1, 10, 0, 0, 0, ZoneOffset.UTC);
        OffsetDateTime t2 = OffsetDateTime.of(2025, 1, 1, 10, 5, 0, 0, ZoneOffset.UTC);
        OffsetDateTime t3 = OffsetDateTime.of(2025, 1, 1, 10, 10, 0, 0, ZoneOffset.UTC);

        saveHistory(account, new BigDecimal("100.00"), t2);
        saveHistory(account, new BigDecimal("90.00"), t1);
        saveHistory(account, new BigDecimal("110.00"), t3);

        Page<BalanceHistory> page = balanceHistoryRepository.findByAccount_IdOrderByCreatedAtAsc(
                account.getId(),
                PageRequest.of(0, 10)
        );

        assertEquals(3, page.getTotalElements());
        assertEquals(t1.toInstant(), page.getContent().get(0).getCreatedAt());
        assertEquals(t2.toInstant(), page.getContent().get(1).getCreatedAt());
        assertEquals(t3.toInstant(), page.getContent().get(2).getCreatedAt());
    }

    @Test
    void findByAccount_IdAndCreatedAtBetweenOrderByCreatedAtAsc_shouldFilterBetweenAndOrderAsc() {
        User user = saveUser("bh2@a.com");
        Account account = saveAccount(user, "BH-ACC-2");

        OffsetDateTime t0 = OffsetDateTime.of(2025, 1, 1, 9, 59, 0, 0, ZoneOffset.UTC);
        OffsetDateTime t1 = OffsetDateTime.of(2025, 1, 1, 10, 0, 0, 0, ZoneOffset.UTC);
        OffsetDateTime t2 = OffsetDateTime.of(2025, 1, 1, 10, 5, 0, 0, ZoneOffset.UTC);
        OffsetDateTime t3 = OffsetDateTime.of(2025, 1, 1, 10, 10, 0, 0, ZoneOffset.UTC);
        OffsetDateTime t4 = OffsetDateTime.of(2025, 1, 1, 10, 11, 0, 0, ZoneOffset.UTC);

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
        assertEquals(t1.toInstant(), page.getContent().get(0).getCreatedAt());
        assertEquals(t2.toInstant(), page.getContent().get(1).getCreatedAt());
        assertEquals(t3.toInstant(), page.getContent().get(2).getCreatedAt());
    }

    @Test
    void existsByAccount_IdAndCreatedAtAfter_shouldWork() {
        User user = saveUser("bh3@a.com");
        Account account = saveAccount(user, "BH-ACC-3");

        OffsetDateTime t1 = OffsetDateTime.of(2025, 1, 1, 10, 0, 0, 0, ZoneOffset.UTC);
        OffsetDateTime t2 = OffsetDateTime.of(2025, 1, 1, 11, 0, 0, 0, ZoneOffset.UTC);

        saveHistory(account, new BigDecimal("10.00"), t1);
        saveHistory(account, new BigDecimal("20.00"), t2);

        assertTrue(balanceHistoryRepository.existsByAccount_IdAndCreatedAtAfter(
                account.getId(),
                OffsetDateTime.of(2025, 1, 1, 10, 30, 0, 0, ZoneOffset.UTC)
        ));

        assertFalse(balanceHistoryRepository.existsByAccount_IdAndCreatedAtAfter(
                account.getId(),
                OffsetDateTime.of(2025, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC)
        ));

        assertFalse(balanceHistoryRepository.existsByAccount_IdAndCreatedAtAfter(
                account.getId(),
                t2
        ));
    }

    private User saveUser(String email) {
        User user = User.builder()
                .firstName("Test")
                .lastName("User")
                .email(email)
                .phone("000")
                .passwordHash("HASH")
                .role("USER")
                .build();
        return userRepository.saveAndFlush(user);
    }

    private Account saveAccount(User user, String accountNumber) {
        Account account = Account.builder()
                .user(user)
                .accountNumber(accountNumber)
                .balance(BigDecimal.ZERO)
                .currency("HUF")
                .accountType(AccountType.CURRENT)
                .status(AccountStatus.ACTIVE)
                .cardNumber("4895121234567890")
                .build();
        return accountRepository.saveAndFlush(account);
    }

    private BalanceHistory saveHistory(Account account, BigDecimal balance, OffsetDateTime createdAt) {
        BalanceHistory history = BalanceHistory.builder()
                .account(account)
                .balance(balance)
                .createdAt(createdAt.toInstant())
                .build();
        return balanceHistoryRepository.saveAndFlush(history);
    }
}
