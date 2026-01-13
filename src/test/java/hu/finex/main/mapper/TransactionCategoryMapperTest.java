package hu.finex.main.mapper;

import hu.finex.main.dto.TransactionCategoryListItemResponse;
import hu.finex.main.dto.TransactionCategoryResponse;
import hu.finex.main.model.Category;
import hu.finex.main.model.Transaction;
import hu.finex.main.model.TransactionCategory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TransactionCategoryMapperTest {

    private final TransactionCategoryMapper mapper = new TransactionCategoryMapper();

    @Test
    void testToEntity() {
        Transaction transaction = Transaction.builder()
                .id(100L)
                .build();

        Category category = Category.builder()
                .id(5L)
                .name("Food")
                .icon("🍔")
                .build();

        TransactionCategory tc = mapper.toEntity(transaction, category);

        assertNotNull(tc);
        assertNull(tc.getId());
        assertEquals(transaction, tc.getTransaction());
        assertEquals(category, tc.getCategory());
    }

    @Test
    void testToResponse() {
        Transaction transaction = Transaction.builder()
                .id(200L)
                .build();

        Category category = Category.builder()
                .id(8L)
                .name("Transport")
                .icon("🚗")
                .build();

        TransactionCategory tc = TransactionCategory.builder()
                .id(50L)
                .transaction(transaction)
                .category(category)
                .build();

        TransactionCategoryResponse response = mapper.toResponse(tc);

        assertNotNull(response);
        assertEquals(50L, response.getId());
        assertEquals(200L, response.getTransactionId());
        assertEquals(8L, response.getCategoryId());
        assertEquals("Transport", response.getCategoryName());
        assertEquals("🚗", response.getCategoryIcon());
    }

    @Test
    void testToListItem() {
        Category category = Category.builder()
                .id(12L)
                .name("Entertainment")
                .icon("🎬")
                .build();

        TransactionCategory tc = TransactionCategory.builder()
                .category(category)
                .build();

        TransactionCategoryListItemResponse response = mapper.toListItem(tc);

        assertNotNull(response);
        assertEquals(12L, response.getCategoryId());
        assertEquals("Entertainment", response.getCategoryName());
        assertEquals("🎬", response.getCategoryIcon());
    }
}
