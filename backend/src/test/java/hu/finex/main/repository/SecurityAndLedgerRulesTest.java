package hu.finex.main.repository;

import hu.finex.main.dto.MonthlySummaryResponse;
import hu.finex.main.dto.ReconciliationItemResponse;
import hu.finex.main.model.Account;
import hu.finex.main.model.SavingsAccount;
import hu.finex.main.model.User;
import hu.finex.main.model.enums.AccountStatus;
import hu.finex.main.model.enums.AccountType;
import hu.finex.main.model.enums.SavingsStatus;
import hu.finex.main.model.enums.UserRole;
import hu.finex.main.model.enums.UserStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

// A szabálysértő utasítás mindig az utolsó lépés, mert utána a tranzakció "aborted" állapotú

@DataJpaTest
@ActiveProfiles("test")
@Import(ReportRepository.class)
class SecurityAndLedgerRulesTest extends PostgresRepositoryTestBase {

    @Autowired private JdbcTemplate jdbcTemplate;
    @Autowired private ReportRepository reportRepository;
    @Autowired private AccountRepository accountRepository;
    @Autowired private SavingsAccountRepository savingsAccountRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private IdempotencyKeyRepository idempotencyKeyRepository;

    @Test
    void transactionCurrency_mustMatchTheAccount() {
        Account account = saveAccount(saveUser("s1@test.hu"), "SEC-1", "HUF", "0.00");

        assertThrows(DataIntegrityViolationException.class,
                () -> jdbcTemplate.update("insert into transactions (account_id, type, amount, currency) values (?, 'INCOME', 10, 'EUR')", account.getId()));
    }

    @Test
    void balanceHistory_shouldNotBeUpdatable() {
        Account account = saveAccount(saveUser("s2@test.hu"), "SEC-2", "HUF", "100.00");
        jdbcTemplate.update("insert into balance_history (account_id, balance) values (?, 100)", account.getId());
        Long id = jdbcTemplate.queryForObject("select id from balance_history where account_id = ?", Long.class, account.getId());

        assertThrows(DataIntegrityViolationException.class, () -> jdbcTemplate.update("update balance_history set balance = 1 where id = ?", id));
    }

    @Test
    void auditLog_shouldNotBeDeletable() {
        User admin = saveUser("s3@test.hu");
        jdbcTemplate.update("insert into audit_log (actor_id, actor_email, action, target_type, target_id) values (?, ?, 'USER_STATUS_CHANGED', 'USER', 1)",
                admin.getId(), admin.getEmail());
        Long id = jdbcTemplate.queryForObject("select id from audit_log where actor_id = ?", Long.class, admin.getId());

        assertThrows(DataIntegrityViolationException.class, () -> jdbcTemplate.update("delete from audit_log where id = ?", id));
    }

    @Test
    void savingsInterest_canBeCreditedOnlyOncePerMonth() {
        SavingsAccount savings = saveSavings(saveUser("s4@test.hu"));
        insertSavingsTransaction(savings, "INTEREST", "10.00", "1010.00", "2026-03-02T10:00:00Z");

        assertThrows(DataIntegrityViolationException.class,
                () -> insertSavingsTransaction(savings, "INTEREST", "10.00", "1020.00", "2026-03-20T10:00:00Z"));
    }

    @Test
    void savingsMinBalance_shouldUseTheLowestBalanceOfThePeriod() {
        SavingsAccount savings = saveSavings(saveUser("s5@test.hu"));
        insertSavingsTransaction(savings, "DEPOSIT", "1000.00", "1000.00", "2026-01-15T10:00:00Z");
        // Februárban: befizetés 1500-ra, kivét 300-ra, majd hó végi befizetés 800-ra
        insertSavingsTransaction(savings, "DEPOSIT", "500.00", "1500.00", "2026-02-03T10:00:00Z");
        insertSavingsTransaction(savings, "WITHDRAWAL", "1200.00", "300.00", "2026-02-10T10:00:00Z");
        insertSavingsTransaction(savings, "DEPOSIT", "500.00", "800.00", "2026-02-27T10:00:00Z");

        BigDecimal february = savingsAccountRepository.findMinBalance(savings.getId(), Instant.parse("2026-02-01T00:00:00Z"), Instant.parse("2026-03-01T00:00:00Z"));
        BigDecimal march = savingsAccountRepository.findMinBalance(savings.getId(), Instant.parse("2026-03-01T00:00:00Z"), Instant.parse("2026-04-01T00:00:00Z"));
        BigDecimal december = savingsAccountRepository.findMinBalance(savings.getId(), Instant.parse("2025-12-01T00:00:00Z"), Instant.parse("2026-01-01T00:00:00Z"));

        assertEquals(0, new BigDecimal("300.00").compareTo(february));
        // Tétel nélküli hónap: az időszak eleji egyenleg
        assertEquals(0, new BigDecimal("800.00").compareTo(march));
        // A megnyitás előtt még nem volt pénz rajta
        assertEquals(0, BigDecimal.ZERO.compareTo(december));
    }

    @Test
    void monthlySummary_shouldIgnoreOwnTransfers_andReversals() {
        User user = saveUser("s6@test.hu");
        Account main = saveAccount(user, "SEC-6A", "HUF", "0.00");
        Account second = saveAccount(user, "SEC-6B", "HUF", "0.00");

        // Valódi kiadás: külső utalás 1000
        insertTransaction(main, "TRANSFER_OUT", "1000.00", null, "HU-KULSO", null, "2026-05-02T10:00:00Z");
        // Saját számlák közötti átvezetés 5000 (mindkét oldala kimarad)
        long out = insertTransaction(main, "TRANSFER_OUT", "5000.00", "SEC-6A", "SEC-6B", null, "2026-05-03T10:00:00Z");
        insertTransaction(second, "TRANSFER_IN", "5000.00", "SEC-6A", "SEC-6B", null, "2026-05-03T10:00:00Z");
        // Kártyás fizetés 700 és a sztornója (mindkettő kimarad)
        long card = insertTransaction(main, "OUTCOME", "700.00", "SEC-6A", null, null, "2026-05-04T10:00:00Z");
        insertTransaction(main, "INCOME", "700.00", null, "SEC-6A", card, "2026-05-05T10:00:00Z");

        List<MonthlySummaryResponse> months = reportRepository.findMonthlySummary(List.of(main.getId(), second.getId()), LocalDate.of(2026, 5, 1));

        assertEquals(1, months.size());
        assertEquals(0, BigDecimal.ZERO.compareTo(months.get(0).getIncome()));
        assertEquals(0, new BigDecimal("1000.00").compareTo(months.get(0).getOutcome()));
        assertTrue(out > 0);
    }

    @Test
    void reconciliation_shouldReportOnlyAccountsWhoseBalanceDiffersFromTheLedger() {
        User user = saveUser("s7@test.hu");
        Account ok = saveAccount(user, "SEC-7A", "HUF", "1000.00");
        insertTransaction(ok, "INCOME", "1000.00", null, "SEC-7A", null, "2026-05-01T10:00:00Z");
        Account broken = saveAccount(user, "SEC-7B", "HUF", "1000.00");
        insertTransaction(broken, "INCOME", "800.00", null, "SEC-7B", null, "2026-05-01T10:00:00Z");

        List<ReconciliationItemResponse> mismatches = reportRepository.findReconciliationMismatches();

        assertTrue(mismatches.stream().noneMatch(item -> item.getId().equals(ok.getId()) && "ACCOUNT".equals(item.getKind())));
        ReconciliationItemResponse item = mismatches.stream().filter(m -> m.getId().equals(broken.getId()) && "ACCOUNT".equals(m.getKind())).findFirst().orElseThrow();
        assertEquals(0, new BigDecimal("200.00").compareTo(item.getDifference()));
    }

    @Test
    void idempotencyKey_canBeReservedOnlyOnce() {
        User user = saveUser("s8@test.hu");

        assertEquals(1, idempotencyKeyRepository.insertIfAbsent(user.getId(), "kulcs-12345678", "hash"));
        assertEquals(0, idempotencyKeyRepository.insertIfAbsent(user.getId(), "kulcs-12345678", "hash"));
        assertTrue(idempotencyKeyRepository.findByUserIdAndIdempotencyKey(user.getId(), "kulcs-12345678").isPresent());
    }

    @Test
    void transaction_canBeReversedOnlyOnce() {
        Account account = saveAccount(saveUser("s9@test.hu"), "SEC-9", "HUF", "0.00");
        long original = insertTransaction(account, "OUTCOME", "100.00", "SEC-9", null, null, "2026-05-01T10:00:00Z");
        insertTransaction(account, "INCOME", "100.00", null, "SEC-9", original, "2026-05-02T10:00:00Z");

        assertThrows(DataIntegrityViolationException.class,
                () -> insertTransaction(account, "INCOME", "100.00", null, "SEC-9", original, "2026-05-03T10:00:00Z"));
    }

    private long insertTransaction(Account account, String type, String amount, String fromAccount, String toAccount, Long reversalOf, String createdAt) {
        return jdbcTemplate.queryForObject(
                "insert into transactions (account_id, type, amount, currency, from_account, to_account, reversal_of_id, created_at)"
                        + " values (?, ?, ?, ?, ?, ?, ?, ?) returning id",
                Long.class, account.getId(), type, new BigDecimal(amount), account.getCurrency(), fromAccount, toAccount, reversalOf, Timestamp.from(Instant.parse(createdAt)));
    }

    private void insertSavingsTransaction(SavingsAccount savings, String type, String amount, String balanceAfter, String createdAt) {
        jdbcTemplate.update("insert into savings_transactions (savings_account_id, type, amount, balance_after, created_at) values (?, ?, ?, ?, ?)",
                savings.getId(), type, new BigDecimal(amount), new BigDecimal(balanceAfter), Timestamp.from(Instant.parse(createdAt)));
    }

    private User saveUser(String email) {
        User user = User.builder()
                .firstName("Test")
                .lastName("User")
                .email(email)
                .passwordHash("HASH")
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        return userRepository.saveAndFlush(user);
    }

    private Account saveAccount(User user, String accountNumber, String currency, String balance) {
        Account account = Account.builder()
                .user(user)
                .name("Teszt számla")
                .accountNumber(accountNumber)
                .balance(new BigDecimal(balance))
                .currency(currency)
                .accountType(AccountType.CURRENT)
                .status(AccountStatus.ACTIVE)
                .createdAt(Instant.now())
                .build();
        return accountRepository.saveAndFlush(account);
    }

    private SavingsAccount saveSavings(User user) {
        SavingsAccount savings = SavingsAccount.builder()
                .user(user)
                .name("Teszt cél")
                .balance(BigDecimal.ZERO)
                .currency("HUF")
                .interestRate(new BigDecimal("3.50"))
                .status(SavingsStatus.ACTIVE)
                .createdAt(Instant.parse("2026-01-10T10:00:00Z"))
                .updatedAt(Instant.now())
                .build();
        return savingsAccountRepository.saveAndFlush(savings);
    }
}
