package hu.finex.main.service;

import hu.finex.main.exception.BusinessException;
import hu.finex.main.model.Account;
import hu.finex.main.model.User;
import hu.finex.main.model.enums.AccountStatus;
import hu.finex.main.model.enums.AccountType;
import hu.finex.main.model.enums.TransactionType;
import hu.finex.main.model.enums.UserRole;
import hu.finex.main.model.enums.UserStatus;
import hu.finex.main.repository.AccountRepository;
import hu.finex.main.repository.PostgresRepositoryTestBase;
import hu.finex.main.repository.TransactionRepository;
import hu.finex.main.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.*;

// A FOR UPDATE sorzárolás és a rögzített zárolási sorrend véd a mínusz és a holtpont ellen

@SpringBootTest
@DirtiesContext
class TransferConcurrencyIntegrationTest extends PostgresRepositoryTestBase {

    @Autowired private TransactionService transactionService;
    @Autowired private UserRepository userRepository;
    @Autowired private AccountRepository accountRepository;
    @Autowired private TransactionRepository transactionRepository;

    @Test
    void parallelTransfers_shouldNeverOverdrawTheAccount() throws Exception {
        User sender = saveUser("sender@test.hu");
        User receiver = saveUser("receiver@test.hu");
        Account from = saveAccount(sender, "HU15117730161111101800000001", "50000.00");
        Account to = saveAccount(receiver, "HU28104000950000521700000003", "0.00");

        // 10 egyidejű 10 000 Ft-os utalás, de csak 50 000 Ft fedezet: pontosan 5 sikerülhet
        List<Callable<Boolean>> tasks = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            tasks.add(() -> tryTransfer(sender, from.getId(), to.getAccountNumber(), "10000.00"));
        }
        List<Boolean> results = runConcurrently(tasks);

        long succeeded = results.stream().filter(Boolean::booleanValue).count();
        Account fromAfter = accountRepository.findById(from.getId()).orElseThrow();
        Account toAfter = accountRepository.findById(to.getId()).orElseThrow();

        assertEquals(5, succeeded);
        assertEquals(0, BigDecimal.ZERO.compareTo(fromAfter.getBalance()));
        assertEquals(0, new BigDecimal("50000.00").compareTo(toAfter.getBalance()));
        assertEquals(0, new BigDecimal("50000.00").compareTo(transactionRepository.sumAmountByAccountAndTypeSince(from.getId(), TransactionType.TRANSFER_OUT, Instant.EPOCH)));
    }

    @Test
    void oppositeTransfers_shouldNotDeadlock_andKeepTheTotal() throws Exception {
        User first = saveUser("first@test.hu");
        User second = saveUser("second@test.hu");
        Account a = saveAccount(first, "HU50120100070000123400000004", "10000.00");
        Account b = saveAccount(second, "HU85117730161111101800000002", "10000.00");

        List<Callable<Boolean>> tasks = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            if (i % 2 == 0) {
                tasks.add(() -> tryTransfer(first, a.getId(), b.getAccountNumber(), "1000.00"));
            } else {
                tasks.add(() -> tryTransfer(second, b.getId(), a.getAccountNumber(), "1000.00"));
            }
        }
        List<Boolean> results = runConcurrently(tasks);

        Account aAfter = accountRepository.findById(a.getId()).orElseThrow();
        Account bAfter = accountRepository.findById(b.getId()).orElseThrow();

        assertTrue(results.stream().allMatch(Boolean::booleanValue));
        assertEquals(0, new BigDecimal("20000.00").compareTo(aAfter.getBalance().add(bAfter.getBalance())));
        assertEquals(0, new BigDecimal("10000.00").compareTo(aAfter.getBalance()));
    }

    private boolean tryTransfer(User owner, Long fromAccountId, String toAccountNumber, String amount) {
        try {
            transactionService.executeTransfer(owner, fromAccountId, toAccountNumber, "Teszt", new BigDecimal(amount), "Párhuzamos teszt", null);
            return true;
        } catch (BusinessException e) {
            return false;
        }
    }

    private List<Boolean> runConcurrently(List<Callable<Boolean>> tasks) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(tasks.size());
        CountDownLatch start = new CountDownLatch(1);
        try {
            List<Future<Boolean>> futures = new ArrayList<>();
            for (Callable<Boolean> task : tasks) {
                futures.add(executor.submit(() -> {
                    start.await();
                    return task.call();
                }));
            }
            start.countDown();

            List<Boolean> results = new ArrayList<>();
            for (Future<Boolean> future : futures) {
                results.add(future.get());
            }
            return results;
        } finally {
            executor.shutdownNow();
        }
    }

    private User saveUser(String email) {
        User user = User.builder()
                .firstName("Teszt")
                .lastName("Felhasználó")
                .email(email)
                .passwordHash("HASH")
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .build();
        return userRepository.save(user);
    }

    private Account saveAccount(User user, String accountNumber, String balance) {
        Account account = Account.builder()
                .user(user)
                .name("Fő számla")
                .accountNumber(accountNumber)
                .balance(new BigDecimal(balance))
                .currency("HUF")
                .accountType(AccountType.CURRENT)
                .status(AccountStatus.ACTIVE)
                .build();
        return accountRepository.save(account);
    }
}
