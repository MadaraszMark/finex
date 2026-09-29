package hu.finex.main.mapper;

import hu.finex.main.dto.SavingsTransactionResponse;
import hu.finex.main.model.SavingsAccount;
import hu.finex.main.model.SavingsTransaction;
import hu.finex.main.model.enums.SavingsTransactionType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class SavingsTransactionMapperTest {

    private final SavingsTransactionMapper mapper = new SavingsTransactionMapper();

    @Test
    void testToEntity_shouldStoreBalanceAfterFromSavingsAccount() {
        SavingsAccount savings = SavingsAccount.builder()
                .id(4L)
                .balance(new BigDecimal("35000.00"))
                .build();

        SavingsTransaction transaction = mapper.toEntity(savings, SavingsTransactionType.DEPOSIT, new BigDecimal("5000.00"));

        assertNotNull(transaction);
        assertNull(transaction.getId());
        assertEquals(savings, transaction.getSavingsAccount());
        assertEquals(SavingsTransactionType.DEPOSIT, transaction.getType());
        assertEquals(new BigDecimal("5000.00"), transaction.getAmount());
        assertEquals(new BigDecimal("35000.00"), transaction.getBalanceAfter());
    }

    @Test
    void testToResponse() {
        Instant createdAt = Instant.parse("2025-02-01T00:15:00Z");

        SavingsTransaction transaction = SavingsTransaction.builder()
                .id(12L)
                .type(SavingsTransactionType.INTEREST)
                .amount(new BigDecimal("102.08"))
                .balanceAfter(new BigDecimal("35102.08"))
                .createdAt(createdAt)
                .build();

        SavingsTransactionResponse response = mapper.toResponse(transaction);

        assertEquals(12L, response.getId());
        assertEquals(SavingsTransactionType.INTEREST, response.getType());
        assertEquals(new BigDecimal("102.08"), response.getAmount());
        assertEquals(new BigDecimal("35102.08"), response.getBalanceAfter());
        assertEquals(createdAt, response.getCreatedAt());
    }
}
