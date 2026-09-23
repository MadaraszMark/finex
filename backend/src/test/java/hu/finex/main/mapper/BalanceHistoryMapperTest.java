package hu.finex.main.mapper;

import hu.finex.main.dto.BalanceHistoryListItemResponse;
import hu.finex.main.dto.BalanceHistoryResponse;
import hu.finex.main.model.Account;
import hu.finex.main.model.BalanceHistory;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class BalanceHistoryMapperTest {

    private final BalanceHistoryMapper mapper = new BalanceHistoryMapper();

    @Test
    void testToEntity() {
        Account account = Account.builder().id(15L).build();

        BigDecimal balance = new BigDecimal("1250.50");

        BalanceHistory history = mapper.toEntity(account, balance);

        assertNotNull(history);
        assertNull(history.getId());
        assertEquals(account, history.getAccount());
        assertEquals(balance, history.getBalance());
    }

    @Test
    void testToResponse() {
        Instant createdAt = Instant.parse("2025-01-02T08:30:00Z");

        Account account = Account.builder()
                .id(7L)
                .build();

        BalanceHistory history = BalanceHistory.builder()
                .id(99L)
                .account(account)
                .balance(new BigDecimal("2000.00"))
                .createdAt(createdAt)
                .build();

        BalanceHistoryResponse response = mapper.toResponse(history);

        assertNotNull(response);
        assertEquals(99L, response.getId());
        assertEquals(7L, response.getAccountId());
        assertEquals(new BigDecimal("2000.00"), response.getBalance());
        assertEquals(createdAt, response.getCreatedAt());
    }

    @Test
    void testToListItem() {
        Instant createdAt = Instant.parse("2025-01-03T14:45:00Z");

        BalanceHistory history = BalanceHistory.builder()
                .balance(new BigDecimal("750.25"))
                .createdAt(createdAt)
                .build();

        BalanceHistoryListItemResponse response = mapper.toListItem(history);

        assertNotNull(response);
        assertEquals(new BigDecimal("750.25"), response.getBalance());
        assertEquals(createdAt, response.getCreatedAt());
    }
}
