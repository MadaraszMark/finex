package hu.finex.main.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hu.finex.main.dto.NotificationResponse;
import hu.finex.main.dto.UnreadCountResponse;
import hu.finex.main.exception.NotFoundException;
import hu.finex.main.mapper.NotificationMapper;
import hu.finex.main.model.Notification;
import hu.finex.main.model.User;
import hu.finex.main.model.enums.NotificationType;
import hu.finex.main.repository.NotificationRepository;
import hu.finex.main.security.CurrentUser;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;
    private final CurrentUser currentUser;

    // Más service-ek hívják egy esemény után (pl. beérkező utalás), a hívó tranzakciójában mentődik
    @Transactional
    public void notify(User user, NotificationType type, String title, String message) {
        notificationRepository.save(notificationMapper.toEntity(user, type, title, message));
    }

    @Transactional(readOnly = true)
    public Page<NotificationResponse> listMine(Pageable pageable) {
        return notificationRepository.findByUser_IdOrderByCreatedAtDesc(currentUser.requireId(), pageable).map(notificationMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public UnreadCountResponse countUnread() {
        long count = notificationRepository.countByUser_IdAndReadFalse(currentUser.requireId());
        return UnreadCountResponse.builder().count(count).build();
    }

    @Transactional
    public NotificationResponse markAsRead(Long id) {
        Notification notification = notificationRepository.findByIdAndUser_Id(id, currentUser.requireId()).orElseThrow(() -> new NotFoundException("Értesítés nem található."));

        notification.setRead(true);

        return notificationMapper.toResponse(notification);
    }

    @Transactional
    public void markAllAsRead() {
        notificationRepository.markAllAsRead(currentUser.requireId());
    }
}
