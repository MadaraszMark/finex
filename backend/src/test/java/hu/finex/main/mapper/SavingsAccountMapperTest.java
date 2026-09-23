package hu.finex.main.mapper;

import hu.finex.main.dto.CreateSavingsAccountRequest;
import hu.finex.main.dto.SavingsAccountResponse;
import hu.finex.main.dto.UpdateSavingsAccountRequest;
import hu.finex.main.model.SavingsAccount;
import hu.finex.main.model.User;
import hu.finex.main.model.enums.SavingsStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class SavingsAccountMapperTest {

    private final SavingsAccountMapper mapper = new SavingsAccountMapper();

    @Test
    void testToEntity() {
        CreateSavingsAccountRequest request = CreateSavingsAccountRequest.builder()
                .name("Vésztartalék")
                .initialBalance(new BigDecimal("10000.00"))
                .currency("HUF")
                .interestRate(new BigDecimal("3.50"))
                .build();

        User user = User.builder()
                .id(11L)
                .build();

        SavingsAccount entity = mapper.toEntity(request, user);

        assertNotNull(entity);
        assertNull(entity.getId());
        assertEquals(user, entity.getUser());
        assertEquals("Vésztartalék", entity.getName());
        assertEquals(new BigDecimal("10000.00"), entity.getBalance());
        assertEquals("HUF", entity.getCurrency());
        assertEquals(new BigDecimal("3.50"), entity.getInterestRate());
        assertNull(entity.getStatus());
        assertNotNull(entity.getCreatedAt());
        assertNotNull(entity.getUpdatedAt());
    }

    @Test
    void testUpdateEntity() {
        Instant oldUpdatedAt = Instant.parse("2025-01-01T10:00:00Z");

        SavingsAccount entity = SavingsAccount.builder()
                .id(50L)
                .name("Régi név")
                .interestRate(new BigDecimal("1.00"))
                .status(SavingsStatus.FROZEN)
                .updatedAt(oldUpdatedAt)
                .build();

        UpdateSavingsAccountRequest request = UpdateSavingsAccountRequest.builder()
                .name("Új név")
                .interestRate(new BigDecimal("2.25"))
                .status(SavingsStatus.ACTIVE)
                .build();

        mapper.updateEntity(entity, request);

        assertEquals(50L, entity.getId());
        assertEquals("Új név", entity.getName());
        assertEquals(new BigDecimal("2.25"), entity.getInterestRate());
        assertEquals(SavingsStatus.ACTIVE, entity.getStatus());

        assertNotNull(entity.getUpdatedAt());
        assertTrue(entity.getUpdatedAt().isAfter(oldUpdatedAt));
    }

    @Test
    void testToResponse() {
        Instant createdAt = Instant.parse("2025-01-02T08:00:00Z");
        Instant updatedAt = Instant.parse("2025-01-03T09:00:00Z");

        User user = User.builder()
                .id(7L)
                .build();

        SavingsAccount entity = SavingsAccount.builder()
                .id(123L)
                .user(user)
                .name("Nyaralás")
                .balance(new BigDecimal("250000.00"))
                .currency("EUR")
                .interestRate(new BigDecimal("4.10"))
                .status(SavingsStatus.ACTIVE)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();

        SavingsAccountResponse response = mapper.toResponse(entity);

        assertNotNull(response);
        assertEquals(123L, response.getId());
        assertEquals(7L, response.getUserId());
        assertEquals("Nyaralás", response.getName());
        assertEquals(new BigDecimal("250000.00"), response.getBalance());
        assertEquals("EUR", response.getCurrency());
        assertEquals(new BigDecimal("4.10"), response.getInterestRate());
        assertEquals(SavingsStatus.ACTIVE, response.getStatus());
        assertEquals(createdAt, response.getCreatedAt());
        assertEquals(updatedAt, response.getUpdatedAt());
    }
}

