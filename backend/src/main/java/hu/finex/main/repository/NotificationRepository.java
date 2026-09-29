package hu.finex.main.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import hu.finex.main.model.Notification;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // A felhasználó értesítései, a legújabb elöl
    Page<Notification> findByUser_IdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    // Olvasatlan értesítések száma (a csengő ikon jelvényéhez)
    long countByUser_IdAndReadFalse(Long userId);

    // Csak akkor adja vissza, ha a megadott felhasználóé (jogosultság-ellenőrzés)
    Optional<Notification> findByIdAndUser_Id(Long id, Long userId);

    // Összes olvasatlan értesítés olvasottra állítása egyetlen UPDATE-tel
    @Modifying
    @Query("update Notification n set n.read = true where n.user.id = :userId and n.read = false")
    int markAllAsRead(@Param("userId") Long userId);
}
