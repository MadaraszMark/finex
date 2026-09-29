package hu.finex.main.mapper;

import org.springframework.stereotype.Component;

import hu.finex.main.dto.NotificationResponse;
import hu.finex.main.model.Notification;
import hu.finex.main.model.User;
import hu.finex.main.model.enums.NotificationType;

@Component
public class NotificationMapper {

    public Notification toEntity(User user, NotificationType type, String title, String message) {
        return Notification.builder()
                .user(user)
                .type(type)
                .title(title)
                .message(message)
                .read(false)
                .build();
    }

    public NotificationResponse toResponse(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .type(notification.getType())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .read(notification.isRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
