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
                .accountId(3L)
                .initialDeposit(new BigDecimal("10000.00"))
                .targetAmount(new BigDecimal("500000.00"))
                .build();

        User user = User.builder()
                .id(11L)
                .build();

        SavingsAccount entity = mapper.toEntity(request, user, "HUF", new BigDecimal("3.50"));

        assertNotNull(entity);
        assertNull(entity.getId());
        assertEquals(user, entity.getUser());
        assertEquals("Vésztartalék", entity.getName());
        // A kezdő összeg nem az entitásba kerül, hanem befizetésként könyvelődik (service)
        assertEquals(BigDecimal.ZERO, entity.getBalance());
        assertEquals("HUF", entity.getCurrency());
        assertEquals(new BigDecimal("3.50"), entity.getInterestRate());
        assertEquals(new BigDecimal("500000.00"), entity.getTargetAmount());
        assertEquals(SavingsStatus.ACTIVE, entity.getStatus());
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
                .targetAmount(new BigDecimal("100000.00"))
                .status(SavingsStatus.FROZEN)
                .updatedAt(oldUpdatedAt)
                .build();

        UpdateSavingsAccountRequest request = UpdateSavingsAccountRequest.builder()
                .name("Új név")
                .targetAmount(null)
                .build();

        mapper.updateEntity(entity, request);

        assertEquals(50L, entity.getId());
        assertEquals("Új név", entity.getName());
        assertNull(entity.getTargetAmount());
        // A kamatláb és az állapot nem módosítható ezen a kérésen keresztül
        assertEquals(new BigDecimal("1.00"), entity.getInterestRate());
        assertEquals(SavingsStatus.FROZEN, entity.getStatus());

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
                .targetAmount(new BigDecimal("600000.00"))
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
        assertEquals(new BigDecimal("600000.00"), response.getTargetAmount());
        assertEquals(new BigDecimal("41.67"), response.getProgressPercent());
        assertEquals(SavingsStatus.ACTIVE, response.getStatus());
        assertEquals(createdAt, response.getCreatedAt());
        assertEquals(updatedAt, response.getUpdatedAt());
    }

    @Test
    void testToResponse_withoutTarget_shouldHaveNoProgress() {
        SavingsAccount entity = SavingsAccount.builder()
                .id(124L)
                .user(User.builder().id(7L).build())
                .balance(new BigDecimal("1000.00"))
                .build();

        SavingsAccountResponse response = mapper.toResponse(entity);

        assertNull(response.getTargetAmount());
        assertNull(response.getProgressPercent());
    }
}
