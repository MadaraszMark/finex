package hu.finex.main.mapper;

import hu.finex.main.dto.CategoryResponse;
import hu.finex.main.dto.TransactionListItemResponse;
import hu.finex.main.dto.TransactionResponse;
import hu.finex.main.model.Account;
import hu.finex.main.model.Card;
import hu.finex.main.model.Transaction;
import hu.finex.main.model.enums.TransactionType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TransactionMapperTest {

    private final TransactionMapper mapper = new TransactionMapper();

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
                .partnerName("Netlab Kft.")
                .fromAccount("EMPLOYER")
                .toAccount("HU00-1111-2222-3333-4444-5555")
                .currency("HUF")
                .createdAt(createdAt)
                .build();

        TransactionResponse response = mapper.toResponse(transaction);

        assertNotNull(response);
        assertEquals(99L, response.getId());
        assertEquals(3L, response.getAccountId());
        assertNull(response.getCardId());
        assertEquals(TransactionType.INCOME, response.getType());
        assertEquals(new BigDecimal("120000.00"), response.getAmount());
        assertEquals("Salary", response.getMessage());
        assertEquals("Netlab Kft.", response.getPartnerName());
        assertEquals("EMPLOYER", response.getFromAccount());
        assertEquals("HU00-1111-2222-3333-4444-5555", response.getToAccount());
        assertEquals("HUF", response.getCurrency());
        assertTrue(response.getCategories().isEmpty());
        assertEquals(createdAt, response.getCreatedAt());
    }

    @Test
    void testToResponse_withCardAndCategories() {
        Card card = Card.builder()
                .id(7L)
                .build();

        Transaction transaction = Transaction.builder()
                .id(100L)
                .account(Account.builder().id(3L).build())
                .card(card)
                .type(TransactionType.OUTCOME)
                .amount(new BigDecimal("4990.00"))
                .partnerName("Tesco")
                .currency("HUF")
                .build();

        List<CategoryResponse> categories = List.of(CategoryResponse.builder().id(1L).name("Élelmiszer").icon("shopping-cart").build());

        TransactionResponse response = mapper.toResponse(transaction, categories);

        assertEquals(7L, response.getCardId());
        assertEquals(1, response.getCategories().size());
        assertEquals("Élelmiszer", response.getCategories().get(0).getName());
    }

    @Test
    void testToListItem() {
        Instant createdAt = Instant.parse("2025-01-11T08:00:00Z");

        Transaction transaction = Transaction.builder()
                .id(101L)
                .account(Account.builder().id(4L).build())
                .type(TransactionType.OUTCOME)
                .amount(new BigDecimal("1990.00"))
                .message("Coffee")
                .partnerName("Starbucks")
                .currency("HUF")
                .createdAt(createdAt)
                .build();

        List<CategoryResponse> categories = List.of(CategoryResponse.builder().id(2L).name("Étterem, kávézó").icon("utensils").build());

        TransactionListItemResponse response = mapper.toListItem(transaction, categories);

        assertNotNull(response);
        assertEquals(101L, response.getId());
        assertEquals(4L, response.getAccountId());
        assertEquals(TransactionType.OUTCOME, response.getType());
        assertEquals(new BigDecimal("1990.00"), response.getAmount());
        assertEquals("Coffee", response.getMessage());
        assertEquals("Starbucks", response.getPartnerName());
        assertEquals("HUF", response.getCurrency());
        assertEquals(categories, response.getCategories());
        assertEquals(createdAt, response.getCreatedAt());
    }
}
