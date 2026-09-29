package hu.finex.main.service;

import hu.finex.main.exception.BusinessException;
import hu.finex.main.mapper.BalanceHistoryMapper;
import hu.finex.main.model.Account;
import hu.finex.main.model.BalanceHistory;
import hu.finex.main.model.Card;
import hu.finex.main.model.Transaction;
import hu.finex.main.model.enums.TransactionType;
import hu.finex.main.repository.BalanceHistoryRepository;
import hu.finex.main.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LedgerServiceTest {

    @Mock private TransactionRepository transactionRepository;
    @Mock private BalanceHistoryRepository balanceHistoryRepository;
    @Mock private BalanceHistoryMapper balanceHistoryMapper;

    @InjectMocks private LedgerService service;

    @Test
    void credit_shouldIncreaseBalance_andBookTransactionAndHistory() {
        Account account = account("1000.00");
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BalanceHistory history = BalanceHistory.builder().account(account).balance(new BigDecimal("1250.00")).build();
        when(balanceHistoryMapper.toEntity(account, new BigDecimal("1250.00"))).thenReturn(history);

        Transaction tx = service.credit(account, TransactionType.INCOME, new BigDecimal("250.00"), "Netlab Kft.", "Munkabér", "HU00EMPLOYER");

        assertEquals(new BigDecimal("1250.00"), account.getBalance());
        assertEquals(account, tx.getAccount());
        assertEquals(TransactionType.INCOME, tx.getType());
        assertEquals(new BigDecimal("250.00"), tx.getAmount());
        assertEquals("HUF", tx.getCurrency());
        assertEquals("Netlab Kft.", tx.getPartnerName());
        assertEquals("Munkabér", tx.getMessage());
        assertEquals("HU00EMPLOYER", tx.getFromAccount());
        assertEquals("HU15117730161111101800000001", tx.getToAccount());
        assertNull(tx.getCard());
        verify(balanceHistoryRepository).save(history);
    }

    @Test
    void debit_shouldDecreaseBalance_andLinkCard() {
        Account account = account("1000.00");
        Card card = Card.builder().id(3L).build();
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Transaction tx = service.debit(account, TransactionType.OUTCOME, new BigDecimal("999.99"), "Tesco", "Kártyás fizetés", null, card);

        assertEquals(new BigDecimal("0.01"), account.getBalance());
        assertEquals(TransactionType.OUTCOME, tx.getType());
        assertEquals(card, tx.getCard());
        assertEquals("HU15117730161111101800000001", tx.getFromAccount());
        assertNull(tx.getToAccount());
        verify(balanceHistoryMapper).toEntity(account, new BigDecimal("0.01"));
    }

    @Test
    void debit_shouldAllowSpendingTheWholeBalance() {
        Account account = account("500.00");
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.debit(account, TransactionType.TRANSFER_OUT, new BigDecimal("500.00"), "Nagy Bence", null, "HU28104000950000521700000003", null);

        assertEquals(0, BigDecimal.ZERO.compareTo(account.getBalance()));
    }

    @Test
    void debit_shouldThrowBusinessException_whenBalanceIsInsufficient() {
        Account account = account("100.00");

        assertThrows(BusinessException.class, () ->
                service.debit(account, TransactionType.OUTCOME, new BigDecimal("100.01"), "Tesco", null, null, null));

        assertEquals(new BigDecimal("100.00"), account.getBalance());
        verifyNoInteractions(transactionRepository, balanceHistoryRepository);
    }

    @Test
    void credit_shouldThrowBusinessException_whenAmountIsNotPositive() {
        Account account = account("100.00");

        assertThrows(BusinessException.class, () -> service.credit(account, TransactionType.INCOME, BigDecimal.ZERO, null, null, null));
        assertThrows(BusinessException.class, () -> service.credit(account, TransactionType.INCOME, new BigDecimal("-5.00"), null, null, null));
        assertThrows(BusinessException.class, () -> service.credit(account, TransactionType.INCOME, null, null, null, null));

        assertEquals(new BigDecimal("100.00"), account.getBalance());
        verifyNoInteractions(transactionRepository);
    }

    @Test
    void credit_shouldThrowBusinessException_whenAmountHasMoreThanTwoDecimals() {
        Account account = account("100.00");

        assertThrows(BusinessException.class, () -> service.credit(account, TransactionType.INCOME, new BigDecimal("0.001"), null, null, null));

        verifyNoInteractions(transactionRepository);
    }

    @Test
    void credit_shouldAcceptTrailingZeros() {
        Account account = account("100.00");
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.credit(account, TransactionType.INCOME, new BigDecimal("1.5000"), null, null, null);

        assertEquals(0, new BigDecimal("101.50").compareTo(account.getBalance()));
    }

    private Account account(String balance) {
        return Account.builder()
                .id(1L)
                .accountNumber("HU15117730161111101800000001")
                .currency("HUF")
                .balance(new BigDecimal(balance))
                .build();
    }
}
