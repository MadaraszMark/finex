package hu.finex.main.mapper;

import hu.finex.main.dto.CardResponse;
import hu.finex.main.model.Account;
import hu.finex.main.model.Card;
import hu.finex.main.model.enums.CardStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class CardMapperTest {

    private final CardMapper mapper = new CardMapper();

    @Test
    void testToResponse() {
        Instant createdAt = Instant.parse("2025-01-01T10:00:00Z");

        Account account = Account.builder()
                .id(3L)
                .accountNumber("HU15117730161111101800000001")
                .build();

        Card card = Card.builder()
                .id(8L)
                .account(account)
                .cardNumber("4895127301601184")
                .holderName("KOVACS ANNA")
                .expiryDate(LocalDate.of(2029, 1, 31))
                .status(CardStatus.ACTIVE)
                .dailyLimit(new BigDecimal("200000.00"))
                .onlinePaymentEnabled(true)
                .createdAt(createdAt)
                .build();

        CardResponse response = mapper.toResponse(card, new BigDecimal("12990.00"));

        assertNotNull(response);
        assertEquals(8L, response.getId());
        assertEquals(3L, response.getAccountId());
        assertEquals("HU15117730161111101800000001", response.getAccountNumber());
        assertEquals("**** **** **** 1184", response.getMaskedNumber());
        assertEquals("KOVACS ANNA", response.getHolderName());
        assertEquals(LocalDate.of(2029, 1, 31), response.getExpiryDate());
        assertEquals(CardStatus.ACTIVE, response.getStatus());
        assertEquals(new BigDecimal("200000.00"), response.getDailyLimit());
        assertEquals(new BigDecimal("12990.00"), response.getSpentToday());
        assertTrue(response.isOnlinePaymentEnabled());
        assertEquals(createdAt, response.getCreatedAt());
    }

    @Test
    void testMaskCardNumber_shouldKeepOnlyLastFourDigits() {
        String masked = mapper.maskCardNumber("4895121040095014");

        assertEquals("**** **** **** 5014", masked);
        assertFalse(masked.contains("489512"));
    }
}
