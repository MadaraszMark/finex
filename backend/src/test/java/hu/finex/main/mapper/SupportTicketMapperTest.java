package hu.finex.main.mapper;

import hu.finex.main.dto.CreateSupportTicketRequest;
import hu.finex.main.dto.CreateTicketMessageRequest;
import hu.finex.main.dto.SupportTicketListItemResponse;
import hu.finex.main.dto.SupportTicketResponse;
import hu.finex.main.dto.TicketMessageResponse;
import hu.finex.main.dto.UpdateSupportTicketStatusRequest;
import hu.finex.main.model.SupportTicket;
import hu.finex.main.model.SupportTicketMessage;
import hu.finex.main.model.User;
import hu.finex.main.model.enums.TicketStatus;
import hu.finex.main.model.enums.UserRole;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

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
                .firstName("Anna")
                .lastName("Kovács")
                .email("anna@finex.hu")
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

        List<TicketMessageResponse> messages = List.of(TicketMessageResponse.builder().id(1L).authorName("FineX Ügyfélszolgálat").staff(true).message("Letiltottuk.").build());

        SupportTicketResponse response = mapper.toResponse(ticket, messages);

        assertNotNull(response);
        assertEquals(77L, response.getId());
        assertEquals(9L, response.getUserId());
        assertEquals("Kovács Anna", response.getUserFullName());
        assertEquals("anna@finex.hu", response.getUserEmail());
        assertEquals("Kártya tiltás", response.getTitle());
        assertEquals("Szeretném letiltani a kártyámat", response.getMessage());
        assertEquals(TicketStatus.IN_PROGRESS, response.getStatus());
        assertEquals(messages, response.getMessages());
        assertEquals(createdAt, response.getCreatedAt());
        assertEquals(updatedAt, response.getUpdatedAt());
    }

    @Test
    void testToListItem() {
        Instant createdAt = Instant.parse("2025-01-04T11:00:00Z");

        SupportTicket ticket = SupportTicket.builder()
                .id(78L)
                .user(User.builder().id(9L).firstName("Bence").lastName("Nagy").build())
                .title("Utalás késik")
                .status(TicketStatus.OPEN)
                .createdAt(createdAt)
                .updatedAt(createdAt)
                .build();

        SupportTicketListItemResponse response = mapper.toListItem(ticket);

        assertEquals(78L, response.getId());
        assertEquals("Utalás késik", response.getTitle());
        assertEquals("Nagy Bence", response.getUserFullName());
        assertEquals(TicketStatus.OPEN, response.getStatus());
        assertEquals(createdAt, response.getCreatedAt());
    }

    @Test
    void testToMessageEntity() {
        SupportTicket ticket = SupportTicket.builder().id(77L).build();
        User author = User.builder().id(9L).build();

        CreateTicketMessageRequest request = CreateTicketMessageRequest.builder()
                .message("Köszönöm!")
                .build();

        SupportTicketMessage message = mapper.toMessageEntity(ticket, author, request);

        assertNull(message.getId());
        assertEquals(ticket, message.getTicket());
        assertEquals(author, message.getAuthor());
        assertEquals("Köszönöm!", message.getMessage());
    }

    @Test
    void testToMessageResponse_shouldMarkAdminAsStaff() {
        Instant createdAt = Instant.parse("2025-01-06T10:00:00Z");

        User admin = User.builder()
                .id(1L)
                .firstName("Ügyfélszolgálat")
                .lastName("FineX")
                .role(UserRole.ADMIN)
                .build();

        SupportTicketMessage message = SupportTicketMessage.builder()
                .id(5L)
                .author(admin)
                .message("A kártyát letiltottuk.")
                .createdAt(createdAt)
                .build();

        TicketMessageResponse response = mapper.toMessageResponse(message);

        assertEquals(5L, response.getId());
        assertEquals("FineX Ügyfélszolgálat", response.getAuthorName());
        assertTrue(response.isStaff());
        assertEquals("A kártyát letiltottuk.", response.getMessage());
        assertEquals(createdAt, response.getCreatedAt());
    }

    @Test
    void testToMessageResponse_userIsNotStaff() {
        SupportTicketMessage message = SupportTicketMessage.builder()
                .id(6L)
                .author(User.builder().id(9L).firstName("Anna").lastName("Kovács").role(UserRole.USER).build())
                .message("Köszönöm!")
                .build();

        assertFalse(mapper.toMessageResponse(message).isStaff());
    }
}
