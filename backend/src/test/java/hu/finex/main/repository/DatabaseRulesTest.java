package hu.finex.main.repository;

import hu.finex.main.dto.MonthlySummaryResponse;
import hu.finex.main.dto.StatementItemResponse;
import hu.finex.main.model.Account;
import hu.finex.main.model.SavingsAccount;
import hu.finex.main.model.Transaction;
import hu.finex.main.model.User;
import hu.finex.main.model.enums.AccountStatus;
import hu.finex.main.model.enums.AccountType;
import hu.finex.main.model.enums.SavingsStatus;
import hu.finex.main.model.enums.TransactionType;
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
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

// Az adatbázisban megírt szabályok tesztje (V1–V3 migrációk): CHECK megszorítások, részleges egyedi index,
// a könyvelt tételeket védő trigger, a kivonatfüggvények és a statisztikai nézet.
// A szabálysértő utasítás mindig a teszt utolsó lépése, mert utána a PostgreSQL tranzakció "aborted" állapotba kerül.

@DataJpaTest
@ActiveProfiles("test")
@Import(ReportRepository.class)
class DatabaseRulesTest extends PostgresRepositoryTestBase {

    @Autowired private JdbcTemplate jdbcTemplate;
    @Autowired private ReportRepository reportRepository;
    @Autowired private TransactionRepository transactionRepository;
    @Autowired private AccountRepository accountRepository;
    @Autowired private SavingsAccountRepository savingsAccountRepository;
    @Autowired private UserRepository userRepository;

    @Test
    void bookedTransaction_shouldNotBeUpdatable() {
        Account account = saveAccount(saveUser("t1@test.hu"), "RULE-1", "1000.00");
        Transaction transaction = saveTransaction(account, TransactionType.INCOME, "1000.00", "2025-01-01T10:00:00Z");

        assertThrows(DataIntegrityViolationException.class,
                () -> jdbcTemplate.update("update transactions set amount = 1 where id = ?", transaction.getId()));
    }

    @Test
    void bookedTransaction_shouldNotBeDeletable() {
        Account account = saveAccount(saveUser("t2@test.hu"), "RULE-2", "1000.00");
        Transaction transaction = saveTransaction(account, TransactionType.INCOME, "1000.00", "2025-01-01T10:00:00Z");

        assertThrows(DataIntegrityViolationException.class,
                () -> jdbcTemplate.update("delete from transactions where id = ?", transaction.getId()));
    }

    @Test
    void transactionAmount_mustBePositive() {
        Account account = saveAccount(saveUser("t3@test.hu"), "RULE-3", "0.00");

        assertThrows(DataIntegrityViolationException.class,
                () -> jdbcTemplate.update("insert into transactions (account_id, type, amount, currency) values (?, 'INCOME', -5, 'HUF')", account.getId()));
    }

    @Test
    void accountBalance_cannotBeNegative() {
        Account account = saveAccount(saveUser("t4@test.hu"), "RULE-4", "10.00");

        assertThrows(DataIntegrityViolationException.class,
                () -> jdbcTemplate.update("update accounts set balance = -1 where id = ?", account.getId()));
    }

    @Test
    void email_mustBeStoredInLowercase() {
        assertThrows(DataIntegrityViolationException.class, () -> saveUser("Anna@Test.hu"));
    }

    @Test
    void openSavingsNames_mustBeUnique_butClosedOnesDoNotCount() {
        User user = saveUser("t5@test.hu");
        saveSavings(user, "Nyaralás", SavingsStatus.CLOSED);
        saveSavings(user, "Nyaralás", SavingsStatus.ACTIVE);

        assertThrows(DataIntegrityViolationException.class, () -> saveSavings(user, "nyaralás", SavingsStatus.ACTIVE));
    }

    @Test
    void accountBalanceAtAndStatement_shouldCalculateOpeningAndRunningBalance() {
        // Végső egyenleg 1000 = +500 (jan. 1.) -200 (jan. 5.) +700 (febr. 1.)
        Account account = saveAccount(saveUser("t6@test.hu"), "RULE-6", "1000.00");
        saveTransaction(account, TransactionType.INCOME, "500.00", "2025-01-01T10:00:00Z");
        saveTransaction(account, TransactionType.OUTCOME, "200.00", "2025-01-05T10:00:00Z");
        saveTransaction(account, TransactionType.INCOME, "700.00", "2025-02-01T10:00:00Z");

        Instant from = Instant.parse("2025-01-03T00:00:00Z");
        Instant to = Instant.parse("2025-03-01T00:00:00Z");

        BigDecimal opening = reportRepository.findBalanceAt(account.getId(), from);
        List<StatementItemResponse> items = reportRepository.findStatement(account.getId(), from, to);

        assertEquals(0, new BigDecimal("500.00").compareTo(opening));
        assertEquals(2, items.size());
        assertEquals(0, new BigDecimal("-200.00").compareTo(items.get(0).getSignedAmount()));
        assertEquals(0, new BigDecimal("300.00").compareTo(items.get(0).getRunningBalance()));
        assertEquals(TransactionType.INCOME, items.get(1).getType());
        assertEquals(0, new BigDecimal("1000.00").compareTo(items.get(1).getRunningBalance()));
    }

    @Test
    void monthlySummaryView_shouldGroupIncomeAndOutcomePerMonth() {
        Account account = saveAccount(saveUser("t7@test.hu"), "RULE-7", "1000.00");
        saveTransaction(account, TransactionType.INCOME, "500.00", "2025-01-01T10:00:00Z");
        saveTransaction(account, TransactionType.OUTCOME, "200.00", "2025-01-05T10:00:00Z");
        saveTransaction(account, TransactionType.INCOME, "700.00", "2025-02-01T10:00:00Z");

        List<MonthlySummaryResponse> months = reportRepository.findMonthlySummary(List.of(account.getId()), LocalDate.of(2025, 1, 1));

        assertEquals(2, months.size());
        assertEquals("2025-01", months.get(0).getMonth());
        assertEquals(0, new BigDecimal("500.00").compareTo(months.get(0).getIncome()));
        assertEquals(0, new BigDecimal("200.00").compareTo(months.get(0).getOutcome()));
        assertEquals(2, months.get(0).getTransactionCount());
        assertEquals("2025-02", months.get(1).getMonth());
        assertEquals(0, BigDecimal.ZERO.compareTo(months.get(1).getOutcome()));
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

    private Account saveAccount(User user, String accountNumber, String balance) {
        Account account = Account.builder()
                .user(user)
                .name("Teszt számla")
                .accountNumber(accountNumber)
                .balance(new BigDecimal(balance))
                .currency("HUF")
                .accountType(AccountType.CURRENT)
                .status(AccountStatus.ACTIVE)
                .createdAt(Instant.now())
                .build();
        return accountRepository.saveAndFlush(account);
    }

    private Transaction saveTransaction(Account account, TransactionType type, String amount, String createdAt) {
        Transaction transaction = Transaction.builder()
                .account(account)
                .type(type)
                .amount(new BigDecimal(amount))
                .currency("HUF")
                .createdAt(Instant.parse(createdAt))
                .build();
        return transactionRepository.saveAndFlush(transaction);
    }

    private SavingsAccount saveSavings(User user, String name, SavingsStatus status) {
        SavingsAccount savings = SavingsAccount.builder()
                .user(user)
                .name(name)
                .balance(BigDecimal.ZERO)
                .currency("HUF")
                .interestRate(new BigDecimal("3.50"))
                .status(status)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        return savingsAccountRepository.saveAndFlush(savings);
    }
}
