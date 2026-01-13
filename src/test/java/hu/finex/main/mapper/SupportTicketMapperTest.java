package hu.finex.main.mapper;

import hu.finex.main.dto.CreateSupportTicketRequest;
import hu.finex.main.dto.SupportTicketResponse;
import hu.finex.main.dto.UpdateSupportTicketStatusRequest;
import hu.finex.main.model.SupportTicket;
import hu.finex.main.model.User;
import hu.finex.main.model.enums.TicketStatus;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class SupportTicketMapperTest {

    private final SupportTicketMapper mapper = new SupportTicketMapper();

    @Test
    void testToEntity() {
        CreateSupportTicketRequest request = CreateSupportTicketRequest.builder()
                .title("Bejelentkezési hiba")
                .message("Nem tudok belépni az alkalmazásba")
                .build();

        User user = User.builder()
                .id(4L)
                .build();

        SupportTicket ticket = mapper.toEntity(request, user);

        assertNotNull(ticket);
        assertNull(ticket.getId());
        assertEquals(user, ticket.getUser());
        assertEquals("Bejelentkezési hiba", ticket.getTitle());
        assertEquals("Nem tudok belépni az alkalmazásba", ticket.getMessage());
        assertNull(ticket.getStatus());
    }

    @Test
    void testUpdateStatus() {
        SupportTicket ticket = SupportTicket.builder()
                .id(10L)
                .status(TicketStatus.OPEN)
                .build();

        UpdateSupportTicketStatusRequest request = UpdateSupportTicketStatusRequest.builder()
                .status(TicketStatus.RESOLVED)
                .build();

        mapper.updateStatus(ticket, request);

        assertEquals(10L, ticket.getId());
        assertEquals(TicketStatus.RESOLVED, ticket.getStatus());
    }

    @Test
    void testToResponse() {
        Instant createdAt = Instant.parse("2025-01-04T11:00:00Z");
        Instant updatedAt = Instant.parse("2025-01-05T12:00:00Z");

        User user = User.builder()
                .id(9L)
                .build();

        SupportTicket ticket = SupportTicket.builder()
                .id(77L)
                .user(user)
                .title("Kártya tiltás")
                .message("Szeretném letiltani a kártyámat")
                .status(TicketStatus.IN_PROGRESS)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();

        SupportTicketResponse response = mapper.toResponse(ticket);

        assertNotNull(response);
        assertEquals(77L, response.getId());
        assertEquals(9L, response.getUserId());
        assertEquals("Kártya tiltás", response.getTitle());
        assertEquals("Szeretném letiltani a kártyámat", response.getMessage());
        assertEquals(TicketStatus.IN_PROGRESS, response.getStatus());
        assertEquals(createdAt, response.getCreatedAt());
        assertEquals(updatedAt, response.getUpdatedAt());
    }
}
