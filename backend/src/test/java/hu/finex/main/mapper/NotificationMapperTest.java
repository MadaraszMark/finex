package hu.finex.main.mapper;

import hu.finex.main.dto.NotificationResponse;
import hu.finex.main.model.Notification;
import hu.finex.main.model.User;
import hu.finex.main.model.enums.NotificationType;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class NotificationMapperTest {

    private final NotificationMapper mapper = new NotificationMapper();

    @Test
    void testToEntity_shouldBeUnread() {
        User user = User.builder()
                .id(1L)
                .build();

        Notification notification = mapper.toEntity(user, NotificationType.TRANSACTION, "Beérkező utalás", "15 000 Ft érkezett.");

        assertNotNull(notification);
        assertNull(notification.getId());
        assertEquals(user, notification.getUser());
        assertEquals(NotificationType.TRANSACTION, notification.getType());
        assertEquals("Beérkező utalás", notification.getTitle());
        assertEquals("15 000 Ft érkezett.", notification.getMessage());
        assertFalse(notification.isRead());
    }

    @Test
    void testToResponse() {
        Instant createdAt = Instant.parse("2025-03-05T08:00:00Z");

        Notification notification = Notification.builder()
                .id(9L)
                .type(NotificationType.SECURITY)
                .title("Sikertelen belépések")
                .message("A fiókot 15 percre zároltuk.")
                .read(true)
                .createdAt(createdAt)
                .build();

        NotificationResponse response = mapper.toResponse(notification);

        assertEquals(9L, response.getId());
        assertEquals(NotificationType.SECURITY, response.getType());
        assertEquals("Sikertelen belépések", response.getTitle());
        assertEquals("A fiókot 15 percre zároltuk.", response.getMessage());
        assertTrue(response.isRead());
        assertEquals(createdAt, response.getCreatedAt());
    }
}
