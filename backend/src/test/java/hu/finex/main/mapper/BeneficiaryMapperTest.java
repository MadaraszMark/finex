package hu.finex.main.mapper;

import hu.finex.main.dto.BeneficiaryResponse;
import hu.finex.main.dto.CreateBeneficiaryRequest;
import hu.finex.main.dto.UpdateBeneficiaryRequest;
import hu.finex.main.model.Beneficiary;
import hu.finex.main.model.User;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class BeneficiaryMapperTest {

    private final BeneficiaryMapper mapper = new BeneficiaryMapper();

    @Test
    void testToEntity_shouldUseNormalizedAccountNumber() {
        CreateBeneficiaryRequest request = CreateBeneficiaryRequest.builder()
                .name("Nagy Bence")
                .accountNumber("hu28 1040 0095 0000 5217 0000 0003")
                .note("Kolléga")
                .build();

        User user = User.builder()
                .id(2L)
                .build();

        Beneficiary beneficiary = mapper.toEntity(request, user, "HU28104000950000521700000003");

        assertNotNull(beneficiary);
        assertNull(beneficiary.getId());
        assertEquals(user, beneficiary.getUser());
        assertEquals("Nagy Bence", beneficiary.getName());
        assertEquals("HU28104000950000521700000003", beneficiary.getAccountNumber());
        assertEquals("Kolléga", beneficiary.getNote());
    }

    @Test
    void testUpdateEntity() {
        Beneficiary beneficiary = Beneficiary.builder()
                .id(4L)
                .name("Régi név")
                .accountNumber("HU66109180010000048900240017")
                .note(null)
                .build();

        UpdateBeneficiaryRequest request = UpdateBeneficiaryRequest.builder()
                .name("Tóth Gábor (főbérlő)")
                .accountNumber("HU66 1091 8001 0000 0489 0024 0017")
                .note("Albérlet")
                .build();

        mapper.updateEntity(beneficiary, request, "HU66109180010000048900240017");

        assertEquals(4L, beneficiary.getId());
        assertEquals("Tóth Gábor (főbérlő)", beneficiary.getName());
        assertEquals("HU66109180010000048900240017", beneficiary.getAccountNumber());
        assertEquals("Albérlet", beneficiary.getNote());
    }

    @Test
    void testToResponse() {
        Instant createdAt = Instant.parse("2025-03-01T12:00:00Z");

        Beneficiary beneficiary = Beneficiary.builder()
                .id(4L)
                .name("Nagy Bence")
                .accountNumber("HU28104000950000521700000003")
                .note("Kolléga")
                .createdAt(createdAt)
                .build();

        BeneficiaryResponse response = mapper.toResponse(beneficiary, true);

        assertEquals(4L, response.getId());
        assertEquals("Nagy Bence", response.getName());
        assertEquals("HU28104000950000521700000003", response.getAccountNumber());
        assertEquals("Kolléga", response.getNote());
        assertTrue(response.isInternal());
        assertEquals(createdAt, response.getCreatedAt());
    }
}
