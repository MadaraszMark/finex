package hu.finex.main.repository;

import hu.finex.main.dto.CategorySpendingResponse;
import hu.finex.main.model.Account;
import hu.finex.main.model.Category;
import hu.finex.main.model.Transaction;
import hu.finex.main.model.TransactionCategory;
import hu.finex.main.model.User;
import hu.finex.main.model.enums.AccountStatus;
import hu.finex.main.model.enums.AccountType;
import hu.finex.main.model.enums.TransactionType;
import hu.finex.main.model.enums.UserRole;
import hu.finex.main.model.enums.UserStatus;
import hu.finex.main.util.DateUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class TransactionRepositoryTest extends PostgresRepositoryTestBase {

    private static final PageRequest PAGE = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "createdAt"));
    private static final Instant ALL_FROM = DateUtils.BEGINNING_OF_TIME;
    private static final Instant ALL_TO = DateUtils.END_OF_TIME;

    @Autowired private TransactionRepository transactionRepository;
    @Autowired private TransactionCategoryRepository transactionCategoryRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private AccountRepository accountRepository;
    @Autowired private UserRepository userRepository;

    private User anna;
    private Account annaHuf;
    private Account annaEur;
    private Category food;

    @BeforeEach
    void setUp() {
        anna = saveUser("anna@test.hu");
        annaHuf = saveAccount(anna, "TR-HUF", "HUF");
        annaEur = saveAccount(anna, "TR-EUR", "EUR");

        // A kategóriák a V4 migrációból jönnek (törzsadat)
        food = categoryRepository.findByNameIgnoreCase("Élelmiszer").orElseThrow();

        Transaction groceries = saveTransaction(annaHuf, TransactionType.OUTCOME, "12000.00", "Tesco", "Bevásárlás", "2025-03-03T17:00:00Z");
        transactionCategoryRepository.saveAndFlush(TransactionCategory.builder().transaction(groceries).category(food).build());

        saveTransaction(annaHuf, TransactionType.INCOME, "685000.00", "Netlab Kft.", "Munkabér", "2025-03-05T08:00:00Z");
        saveTransaction(annaHuf, TransactionType.TRANSFER_OUT, "220000.00", "Tóth Gábor", "Albérlet", "2025-03-06T09:00:00Z");
        saveTransaction(annaEur, TransactionType.OUTCOME, "39.99", "Amazon.de", "Online vásárlás", "2025-04-09T12:00:00Z");
    }

    @Test
    void search_shouldReturnOnlyOwnTransactions_whenNoFilter() {
        User bence = saveUser("bence@test.hu");
        Account benceHuf = saveAccount(bence, "TR-BENCE", "HUF");
        saveTransaction(benceHuf, TransactionType.INCOME, "540000.00", "Grafit Studio Bt.", "Munkabér", "2025-03-10T08:00:00Z");

        Page<Transaction> page = transactionRepository.search(anna.getId(), null, null, ALL_FROM, ALL_TO, null, null, null, null, PAGE);

        assertEquals(4, page.getTotalElements());
        assertTrue(page.getContent().stream().allMatch(t -> t.getAccount().getUser().getId().equals(anna.getId())));
        assertEquals("Amazon.de", page.getContent().get(0).getPartnerName());
    }

    @Test
    void search_shouldFilterByAccountTypeAndAmount() {
        Page<Transaction> byAccount = transactionRepository.search(anna.getId(), annaEur.getId(), null, ALL_FROM, ALL_TO, null, null, null, null, PAGE);
        assertEquals(1, byAccount.getTotalElements());

        Page<Transaction> byType = transactionRepository.search(anna.getId(), null, TransactionType.INCOME, ALL_FROM, ALL_TO, null, null, null, null, PAGE);
        assertEquals(1, byType.getTotalElements());
        assertEquals("Munkabér", byType.getContent().get(0).getMessage());

        Page<Transaction> byAmount = transactionRepository.search(anna.getId(), null, null, ALL_FROM, ALL_TO, new BigDecimal("10000"), new BigDecimal("300000"), null, null, PAGE);
        assertEquals(2, byAmount.getTotalElements());
    }

    @Test
    void search_shouldFilterByTextDateAndCategory() {
        Page<Transaction> byText = transactionRepository.search(anna.getId(), null, null, ALL_FROM, ALL_TO, null, null, "%tesco%", null, PAGE);
        assertEquals(1, byText.getTotalElements());

        Page<Transaction> byMessage = transactionRepository.search(anna.getId(), null, null, ALL_FROM, ALL_TO, null, null, "%albérlet%", null, PAGE);
        assertEquals(1, byMessage.getTotalElements());

        Page<Transaction> byDate = transactionRepository.search(anna.getId(), null, null,
                Instant.parse("2025-03-04T00:00:00Z"), Instant.parse("2025-04-01T00:00:00Z"), null, null, null, null, PAGE);
        assertEquals(2, byDate.getTotalElements());

        Page<Transaction> byCategory = transactionRepository.search(anna.getId(), null, null, ALL_FROM, ALL_TO, null, null, null, food.getId(), PAGE);
        assertEquals(1, byCategory.getTotalElements());
        assertEquals("Tesco", byCategory.getContent().get(0).getPartnerName());
    }

    @Test
    void sumAmountByAccountAndTypeSince_shouldSumOnlyMatchingTransactions() {
        saveTransaction(annaHuf, TransactionType.TRANSFER_OUT, "5000.00", "Nagy Bence", "Pizza", "2025-03-06T18:00:00Z");

        BigDecimal sum = transactionRepository.sumAmountByAccountAndTypeSince(annaHuf.getId(), TransactionType.TRANSFER_OUT, Instant.parse("2025-03-06T00:00:00Z"));
        BigDecimal none = transactionRepository.sumAmountByAccountAndTypeSince(annaHuf.getId(), TransactionType.TRANSFER_OUT, Instant.parse("2025-03-07T00:00:00Z"));

        assertEquals(0, new BigDecimal("225000.00").compareTo(sum));
        assertEquals(0, BigDecimal.ZERO.compareTo(none));
    }

    @Test
    void sumByUserAndTypesSince_shouldSumIncomeInCurrency() {
        BigDecimal income = transactionRepository.sumByUserAndTypesSince(anna.getId(), "HUF",
                List.of(TransactionType.INCOME, TransactionType.TRANSFER_IN), Instant.parse("2025-03-01T00:00:00Z"));
        BigDecimal outcome = transactionRepository.sumByUserAndTypesSince(anna.getId(), "HUF",
                List.of(TransactionType.OUTCOME, TransactionType.TRANSFER_OUT), Instant.parse("2025-03-01T00:00:00Z"));

        assertEquals(0, new BigDecimal("685000.00").compareTo(income));
        assertEquals(0, new BigDecimal("232000.00").compareTo(outcome));
    }

    @Test
    void sumSpendingByCategory_shouldGroupByCategory() {
        List<CategorySpendingResponse> spending = transactionCategoryRepository.sumSpendingByCategory(anna.getId(), "HUF",
                List.of(TransactionType.OUTCOME, TransactionType.TRANSFER_OUT), Instant.parse("2025-03-01T00:00:00Z"), Instant.parse("2025-04-01T00:00:00Z"));

        assertEquals(1, spending.size());
        assertEquals("Élelmiszer", spending.get(0).getCategoryName());
        assertEquals(0, new BigDecimal("12000.00").compareTo(spending.get(0).getTotalAmount()));
        assertEquals(1L, spending.get(0).getTransactionCount());
    }

    @Test
    void findByIdAndAccount_User_Id_shouldNotReturnOtherUsersTransaction() {
        User bence = saveUser("bence2@test.hu");
        Transaction annasTransaction = transactionRepository.findAll().stream()
                .filter(t -> t.getAccount().getId().equals(annaHuf.getId()))
                .findFirst()
                .orElseThrow();

        assertTrue(transactionRepository.findByIdAndAccount_User_Id(annasTransaction.getId(), anna.getId()).isPresent());
        assertTrue(transactionRepository.findByIdAndAccount_User_Id(annasTransaction.getId(), bence.getId()).isEmpty());
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

    private Account saveAccount(User user, String accountNumber, String currency) {
        Account account = Account.builder()
                .user(user)
                .name("Teszt számla")
                .accountNumber(accountNumber)
                .balance(new BigDecimal("1000000.00"))
                .currency(currency)
                .accountType(AccountType.CURRENT)
                .status(AccountStatus.ACTIVE)
                .createdAt(Instant.now())
                .build();
        return accountRepository.saveAndFlush(account);
    }

    private Transaction saveTransaction(Account account, TransactionType type, String amount, String partner, String message, String createdAt) {
        Transaction transaction = Transaction.builder()
                .account(account)
                .type(type)
                .amount(new BigDecimal(amount))
                .currency(account.getCurrency())
                .partnerName(partner)
                .message(message)
                .createdAt(Instant.parse(createdAt))
                .build();
        return transactionRepository.saveAndFlush(transaction);
    }
}
