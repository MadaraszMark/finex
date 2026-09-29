package hu.finex.main.service;

import hu.finex.main.dto.NotificationResponse;
import hu.finex.main.dto.UnreadCountResponse;
import hu.finex.main.exception.NotFoundException;
import hu.finex.main.mapper.NotificationMapper;
import hu.finex.main.model.Notification;
import hu.finex.main.model.User;
import hu.finex.main.model.enums.NotificationType;
import hu.finex.main.repository.NotificationRepository;
import hu.finex.main.security.CurrentUser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock private NotificationRepository notificationRepository;
    @Mock private NotificationMapper notificationMapper;
    @Mock private CurrentUser currentUser;

    @InjectMocks private NotificationService service;

    @Test
    void notify_shouldSaveUnreadNotification() {
        User user = User.builder().id(7L).build();
        Notification entity = Notification.builder().user(user).type(NotificationType.SECURITY).title("T").message("M").build();
        when(notificationMapper.toEntity(user, NotificationType.SECURITY, "T", "M")).thenReturn(entity);

        service.notify(user, NotificationType.SECURITY, "T", "M");

        verify(notificationRepository).save(entity);
    }

    @Test
    void listMine_shouldReturnOwnNotificationsPage() {
        when(currentUser.requireId()).thenReturn(7L);

        Pageable pageable = PageRequest.of(0, 20);
        Notification n = Notification.builder().id(1L).build();
        when(notificationRepository.findByUser_IdOrderByCreatedAtDesc(7L, pageable)).thenReturn(new PageImpl<>(List.of(n), pageable, 1));
        when(notificationMapper.toResponse(n)).thenReturn(NotificationResponse.builder().id(1L).build());

        Page<NotificationResponse> page = service.listMine(pageable);

        assertEquals(1, page.getTotalElements());
        assertEquals(1L, page.getContent().get(0).getId());
    }

    @Test
    void countUnread_shouldReturnCount() {
        when(currentUser.requireId()).thenReturn(7L);
        when(notificationRepository.countByUser_IdAndReadFalse(7L)).thenReturn(3L);

        UnreadCountResponse resp = service.countUnread();

        assertEquals(3L, resp.getCount());
    }

    @Test
    void markAsRead_shouldSetReadFlag() {
        when(currentUser.requireId()).thenReturn(7L);

        Notification n = Notification.builder().id(1L).read(false).build();
        when(notificationRepository.findByIdAndUser_Id(1L, 7L)).thenReturn(Optional.of(n));
        when(notificationMapper.toResponse(n)).thenReturn(NotificationResponse.builder().id(1L).read(true).build());

        NotificationResponse resp = service.markAsRead(1L);

        assertTrue(n.isRead());
        assertTrue(resp.isRead());
    }

    @Test
    void markAsRead_shouldThrowNotFound_whenNotOwn() {
        when(currentUser.requireId()).thenReturn(7L);
        when(notificationRepository.findByIdAndUser_Id(1L, 7L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.markAsRead(1L));
    }

    @Test
    void markAllAsRead_shouldUseSingleUpdate() {
        when(currentUser.requireId()).thenReturn(7L);

        service.markAllAsRead();

        verify(notificationRepository).markAllAsRead(7L);
        verifyNoMoreInteractions(notificationRepository);
    }
}
