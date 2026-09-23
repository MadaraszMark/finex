package hu.finex.main.mapper;

import hu.finex.main.dto.CreateSavingsTransactionRequest;
import hu.finex.main.dto.CreateTransactionRequest;
import hu.finex.main.dto.TransactionListItemResponse;
import hu.finex.main.dto.TransactionResponse;
import hu.finex.main.model.Account;
import hu.finex.main.model.Transaction;
import hu.finex.main.model.enums.TransactionType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class TransactionMapperTest {

    private final TransactionMapper mapper = new TransactionMapper();

    @Test
    void testToEntity_fromCreateTransactionRequest() {
        CreateTransactionRequest request = CreateTransactionRequest.builder()
                .type(TransactionType.OUTCOME)
                .amount(new BigDecimal("4990.00"))
                .message("Grocery shopping")
                .fromAccount("HU12-0000-0000-0000-0000-0000")
                .toAccount("SHOP-ACCOUNT-123")
                .currency("HUF")
                .build();

        Account account = Account.builder()
                .id(10L)
                .build();

        Transaction entity = mapper.toEntity(request, account);

        assertNotNull(entity);
        assertNull(entity.getId());
        assertEquals(account, entity.getAccount());
        assertEquals(TransactionType.OUTCOME, entity.getType());
        assertEquals(new BigDecimal("4990.00"), entity.getAmount());
        assertEquals("Grocery shopping", entity.getMessage());
        assertEquals("HU12-0000-0000-0000-0000-0000", entity.getFromAccount());
        assertEquals("SHOP-ACCOUNT-123", entity.getToAccount());
        assertEquals("HUF", entity.getCurrency());
    }

    @Test
    void testToEntity_fromCreateSavingsTransactionRequest() {
        CreateSavingsTransactionRequest request = CreateSavingsTransactionRequest.builder()
                .type(TransactionType.TRANSFER_OUT)
                .amount(new BigDecimal("20000.00"))
                .message("Transfer to savings")
                .fromAccount("MAIN-ACC-001")
                .toAccount("SAVINGS-ACC-777")
                .currency("HUF")
                .build();

        Account account = Account.builder()
                .id(11L)
                .build();

        Transaction entity = mapper.toEntity(request, account);

        assertNotNull(entity);
        assertNull(entity.getId());
        assertEquals(account, entity.getAccount());
        assertEquals(TransactionType.TRANSFER_OUT, entity.getType());
        assertEquals(new BigDecimal("20000.00"), entity.getAmount());
        assertEquals("Transfer to savings", entity.getMessage());
        assertEquals("MAIN-ACC-001", entity.getFromAccount());
        assertEquals("SAVINGS-ACC-777", entity.getToAccount());
        assertEquals("HUF", entity.getCurrency());
    }

    @Test
    void testToResponse() {
        Instant createdAt = Instant.parse("2025-01-10T10:30:00Z");

        Account account = Account.builder()
                .id(3L)
                .build();

        Transaction transaction = Transaction.builder()
                .id(99L)
                .account(account)
                .type(TransactionType.INCOME)
                .amount(new BigDecimal("120000.00"))
                .message("Salary")
                .fromAccount("EMPLOYER")
                .toAccount("HU00-1111-2222-3333-4444-5555")
                .currency("HUF")
                .createdAt(createdAt)
                .build();

        TransactionResponse response = mapper.toResponse(transaction);

        assertNotNull(response);
        assertEquals(99L, response.getId());
        assertEquals(3L, response.getAccountId());
        assertEquals(TransactionType.INCOME, response.getType());
        assertEquals(new BigDecimal("120000.00"), response.getAmount());
        assertEquals("Salary", response.getMessage());
        assertEquals("EMPLOYER", response.getFromAccount());
        assertEquals("HU00-1111-2222-3333-4444-5555", response.getToAccount());
        assertEquals("HUF", response.getCurrency());
        assertEquals(createdAt, response.getCreatedAt());
    }

    @Test
    void testToListItem() {
        Instant createdAt = Instant.parse("2025-01-11T08:00:00Z");

        Transaction transaction = Transaction.builder()
                .id(101L)
                .type(TransactionType.OUTCOME)
                .amount(new BigDecimal("1990.00"))
                .message("Coffee")
                .currency("HUF")
                .createdAt(createdAt)
                .build();

        TransactionListItemResponse response = mapper.toListItem(transaction);

        assertNotNull(response);
        assertEquals(101L, response.getId());
        assertEquals(TransactionType.OUTCOME, response.getType());
        assertEquals(new BigDecimal("1990.00"), response.getAmount());
        assertEquals("Coffee", response.getMessage());
        assertEquals("HUF", response.getCurrency());
        assertEquals(createdAt, response.getCreatedAt());
    }
}
