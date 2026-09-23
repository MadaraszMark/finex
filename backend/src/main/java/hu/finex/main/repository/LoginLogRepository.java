package hu.finex.main.repository;

import java.time.OffsetDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import hu.finex.main.model.LoginLog;
import hu.finex.main.model.enums.LoginStatus;

@Repository
public interface LoginLogRepository extends JpaRepository<LoginLog, Long> {

    // Egy user összes login próbálkozása (admin + user profil)
    Page<LoginLog> findByUser_IdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    // Login státusz alapján (SUCCESS / FAILED)
    Page<LoginLog> findByStatusOrderByCreatedAtDesc(LoginStatus status, Pageable pageable);

    // IP cím alapján keresés – security vizsgálathoz
    Page<LoginLog> findByIpAddressOrderByCreatedAtDesc(String ipAddress, Pageable pageable);

    // User + status kombináció (pl. user failed logins)
    Page<LoginLog> findByUser_IdAndStatusOrderByCreatedAtDesc(Long userId,LoginStatus status,Pageable pageable);

    // Időintervallum alapján (fraud detection)
    Page<LoginLog> findByCreatedAtBetweenOrderByCreatedAtDesc(OffsetDateTime start,OffsetDateTime end,Pageable pageable);

    //  Volt-e sikertelen belépés adott usernél egy időpont után?
    boolean existsByUser_IdAndStatusAndCreatedAtAfter(Long userId,LoginStatus status,OffsetDateTime since);
}

