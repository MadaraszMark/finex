package hu.finex.main.mapper;

import hu.finex.main.dto.CreateStandingOrderRequest;
import hu.finex.main.dto.StandingOrderResponse;
import hu.finex.main.dto.UpdateStandingOrderRequest;
import hu.finex.main.model.Account;
import hu.finex.main.model.StandingOrder;
import hu.finex.main.model.enums.StandingOrderFrequency;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class StandingOrderMapperTest {

    private final StandingOrderMapper mapper = new StandingOrderMapper();

    @Test
    void testToEntity_shouldBeActiveAndStartOnFirstExecutionDate() {
        CreateStandingOrderRequest request = CreateStandingOrderRequest.builder()
                .accountId(1L)
                .toAccountNumber("hu66 1091 8001 0000 0489 0024 0017")
                .partnerName("Tóth Gábor")
                .amount(new BigDecimal("220000.00"))
                .message("Albérlet")
                .frequency(StandingOrderFrequency.MONTHLY)
                .firstExecutionDate(LocalDate.of(2025, 4, 6))
                .build();

        Account account = Account.builder()
                .id(1L)
                .build();

        StandingOrder order = mapper.toEntity(request, account, "HU66109180010000048900240017");

        assertNotNull(order);
        assertNull(order.getId());
        assertEquals(account, order.getAccount());
        assertEquals("HU66109180010000048900240017", order.getToAccountNumber());
        assertEquals("Tóth Gábor", order.getPartnerName());
        assertEquals(new BigDecimal("220000.00"), order.getAmount());
        assertEquals("Albérlet", order.getMessage());
        assertEquals(StandingOrderFrequency.MONTHLY, order.getFrequency());
        assertEquals(LocalDate.of(2025, 4, 6), order.getNextExecutionDate());
        assertTrue(order.isActive());
        assertNull(order.getLastExecutionAt());
    }

    @Test
    void testUpdateEntity() {
        StandingOrder order = StandingOrder.builder()
                .id(3L)
                .toAccountNumber("HU66109180010000048900240017")
                .amount(new BigDecimal("220000.00"))
                .frequency(StandingOrderFrequency.MONTHLY)
                .nextExecutionDate(LocalDate.of(2025, 4, 6))
                .active(true)
                .build();

        UpdateStandingOrderRequest request = UpdateStandingOrderRequest.builder()
                .amount(new BigDecimal("235000.00"))
                .message("Albérlet + rezsi")
                .frequency(StandingOrderFrequency.WEEKLY)
                .nextExecutionDate(LocalDate.of(2025, 5, 6))
                .build();

        mapper.updateEntity(order, request);

        assertEquals(3L, order.getId());
        assertEquals("HU66109180010000048900240017", order.getToAccountNumber());
        assertEquals(new BigDecimal("235000.00"), order.getAmount());
        assertEquals("Albérlet + rezsi", order.getMessage());
        assertEquals(StandingOrderFrequency.WEEKLY, order.getFrequency());
        assertEquals(LocalDate.of(2025, 5, 6), order.getNextExecutionDate());
        assertTrue(order.isActive());
    }

    @Test
    void testToResponse_shouldUseAccountCurrency() {
        Instant createdAt = Instant.parse("2025-01-02T10:00:00Z");
        Instant lastExecutionAt = Instant.parse("2025-03-06T05:05:00Z");

        Account account = Account.builder()
                .id(1L)
                .accountNumber("HU15117730161111101800000001")
                .currency("HUF")
                .build();

        StandingOrder order = StandingOrder.builder()
                .id(3L)
                .account(account)
                .toAccountNumber("HU66109180010000048900240017")
                .partnerName("Tóth Gábor")
                .amount(new BigDecimal("220000.00"))
                .message("Albérlet")
                .frequency(StandingOrderFrequency.MONTHLY)
                .nextExecutionDate(LocalDate.of(2025, 4, 6))
                .active(true)
                .lastExecutionAt(lastExecutionAt)
                .createdAt(createdAt)
                .build();

        StandingOrderResponse response = mapper.toResponse(order);

        assertEquals(3L, response.getId());
        assertEquals(1L, response.getAccountId());
        assertEquals("HU15117730161111101800000001", response.getAccountNumber());
        assertEquals("HU66109180010000048900240017", response.getToAccountNumber());
        assertEquals("Tóth Gábor", response.getPartnerName());
        assertEquals(new BigDecimal("220000.00"), response.getAmount());
        assertEquals("HUF", response.getCurrency());
        assertEquals("Albérlet", response.getMessage());
        assertEquals(StandingOrderFrequency.MONTHLY, response.getFrequency());
        assertEquals(LocalDate.of(2025, 4, 6), response.getNextExecutionDate());
        assertTrue(response.isActive());
        assertEquals(lastExecutionAt, response.getLastExecutionAt());
        assertEquals(createdAt, response.getCreatedAt());
    }
}
