package hu.finex.main.repository;

import hu.finex.main.model.Account;
import hu.finex.main.model.User;
import hu.finex.main.model.enums.AccountStatus;
import hu.finex.main.model.enums.AccountType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class AccountRepositoryTest extends PostgresRepositoryTestBase {

    @Autowired private AccountRepository accountRepository;
    @Autowired private UserRepository userRepository;

    @Test
    void findByAccountNumber_shouldReturnAccount_whenExists() {
        User user = saveUser("a@a.com");
        Account acc = saveAccount(user, "ACC-1", AccountType.CURRENT, AccountStatus.ACTIVE, "HUF", new BigDecimal("100.00"));

        Optional<Account> found = accountRepository.findByAccountNumber("ACC-1");

        assertTrue(found.isPresent());
        assertEquals(acc.getId(), found.get().getId());
        assertEquals("ACC-1", found.get().getAccountNumber());
    }

    @Test
    void findByAccountNumber_shouldReturnEmpty_whenMissing() {
        Optional<Account> found = accountRepository.findByAccountNumber("NOPE");
        assertTrue(found.isEmpty());
    }

    @Test
    void findFirstByUser_IdAndAccountType_shouldReturnMatchingAccount() {
        User user = saveUser("b@a.com");
        saveAccount(user, "ACC-CUR", AccountType.CURRENT, AccountStatus.ACTIVE, "HUF", BigDecimal.ZERO);
        Account savings = saveAccount(user, "ACC-SAV", AccountType.SAVINGS, AccountStatus.ACTIVE, "HUF", BigDecimal.TEN);

        Optional<Account> found = accountRepository.findFirstByUser_IdAndAccountType(user.getId(), AccountType.SAVINGS);

        assertTrue(found.isPresent());
        assertEquals(savings.getId(), found.get().getId());
        assertEquals(AccountType.SAVINGS, found.get().getAccountType());
    }

    @Test
    void findByUser_Id_shouldReturnAllAccountsOfUser() {
        User u1 = saveUser("u1@a.com");
        User u2 = saveUser("u2@a.com");

        saveAccount(u1, "U1-1", AccountType.CURRENT, AccountStatus.ACTIVE, "HUF", BigDecimal.ONE);
        saveAccount(u1, "U1-2", AccountType.SAVINGS, AccountStatus.FROZEN, "HUF", BigDecimal.TEN);
        saveAccount(u2, "U2-1", AccountType.CURRENT, AccountStatus.ACTIVE, "HUF", BigDecimal.ZERO);

        List<Account> accounts = accountRepository.findByUser_Id(u1.getId());

        assertEquals(2, accounts.size());
        assertTrue(accounts.stream().allMatch(a -> a.getUser().getId().equals(u1.getId())));
    }

    @Test
    void findByUser_IdAndStatus_shouldReturnOnlyMatchingStatus() {
        User user = saveUser("c@a.com");

        saveAccount(user, "A1", AccountType.CURRENT, AccountStatus.ACTIVE, "HUF", BigDecimal.ZERO);
        saveAccount(user, "A2", AccountType.SAVINGS, AccountStatus.CLOSED, "HUF", BigDecimal.ZERO);
        saveAccount(user, "A3", AccountType.CREDIT, AccountStatus.ACTIVE, "HUF", BigDecimal.ZERO);

        List<Account> active = accountRepository.findByUser_IdAndStatus(user.getId(), AccountStatus.ACTIVE);

        assertEquals(2, active.size());
        assertTrue(active.stream().allMatch(a -> a.getStatus() == AccountStatus.ACTIVE));
    }

    @Test
    void existsByAccountNumberAndStatus_shouldReturnTrueOnlyWhenBothMatch() {
        User user = saveUser("d@a.com");
        saveAccount(user, "EX-1", AccountType.CURRENT, AccountStatus.ACTIVE, "HUF", BigDecimal.ZERO);

        assertTrue(accountRepository.existsByAccountNumberAndStatus("EX-1", AccountStatus.ACTIVE));
        assertFalse(accountRepository.existsByAccountNumberAndStatus("EX-1", AccountStatus.CLOSED));
        assertFalse(accountRepository.existsByAccountNumberAndStatus("NOPE", AccountStatus.ACTIVE));
    }

    @Test
    void findByAccountNumberStartingWith_shouldReturnOnlyPrefixed() {
        User user = saveUser("e@a.com");

        saveAccount(user, "HU11-AAA", AccountType.CURRENT, AccountStatus.ACTIVE, "HUF", BigDecimal.ZERO);
        saveAccount(user, "HU11-BBB", AccountType.SAVINGS, AccountStatus.ACTIVE, "HUF", BigDecimal.ZERO);
        saveAccount(user, "ACC-XYZ", AccountType.CURRENT, AccountStatus.ACTIVE, "HUF", BigDecimal.ZERO);

        List<Account> hu11 = accountRepository.findByAccountNumberStartingWith("HU11");

        assertEquals(2, hu11.size());
        assertTrue(hu11.stream().allMatch(a -> a.getAccountNumber().startsWith("HU11")));
    }

    @Test
    void findFirstByUser_IdAndStatusAndAccountType_shouldReturnActiveCurrent() {
        User user = saveUser("f@a.com");

        saveAccount(user, "CUR-ACT", AccountType.CURRENT, AccountStatus.ACTIVE, "HUF", BigDecimal.ZERO);
        saveAccount(user, "CUR-FRO", AccountType.CURRENT, AccountStatus.FROZEN, "HUF", BigDecimal.ZERO);
        saveAccount(user, "SAV-ACT", AccountType.SAVINGS, AccountStatus.ACTIVE, "HUF", BigDecimal.ZERO);

        Optional<Account> found = accountRepository.findFirstByUser_IdAndStatusAndAccountType(
                user.getId(), AccountStatus.ACTIVE, AccountType.CURRENT
        );

        assertTrue(found.isPresent());
        assertEquals("CUR-ACT", found.get().getAccountNumber());
        assertEquals(AccountStatus.ACTIVE, found.get().getStatus());
        assertEquals(AccountType.CURRENT, found.get().getAccountType());
    }

    private User saveUser(String email) {
        User user = User.builder()
                .firstName("Test")
                .lastName("User")
                .email(email)
                .phone("000")
                .passwordHash("HASH")
                .role("USER")
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        return userRepository.saveAndFlush(user);
    }

    private Account saveAccount(
            User user,
            String accountNumber,
            AccountType type,
            AccountStatus status,
            String currency,
            BigDecimal balance
    ) {
        Account account = Account.builder()
                .user(user)
                .accountNumber(accountNumber)
                .balance(balance)
                .currency(currency)
                .accountType(type)
                .status(status)
                .cardNumber("4895121234567890")
                .createdAt(Instant.now())
                .build();
        return accountRepository.saveAndFlush(account);
    }
}
