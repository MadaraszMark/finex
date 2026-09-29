package hu.finex.main.repository;

import java.time.Instant;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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
    Page<LoginLog> findByCreatedAtBetweenOrderByCreatedAtDesc(Instant start,Instant end,Pageable pageable);

    //  Volt-e sikertelen belépés adott usernél egy időpont után?
    boolean existsByUser_IdAndStatusAndCreatedAtAfter(Long userId,LoginStatus status,Instant since);

    // Sikertelen próbálkozások száma egy e-mail címre egy időpont óta (átmeneti zároláshoz)
    long countByEmailAndStatusAndCreatedAtAfter(String email, LoginStatus status, Instant since);

    // Az utolsó sikeres belépés (utána a sikertelen próbálkozások számlálója újraindul)
    Optional<LoginLog> findFirstByEmailAndStatusOrderByCreatedAtDesc(String email, LoginStatus status);

    // Admin keresés: a szűrők opcionálisak (null = nincs szűrés), az időszaknak viszont mindig van határa
    @Query("select l from LoginLog l where (:status is null or l.status = :status)"
            + " and (:userId is null or l.user.id = :userId)"
            + " and (:ipAddress is null or l.ipAddress = :ipAddress)"
            + " and l.createdAt >= :from and l.createdAt < :to"
            + " order by l.createdAt desc")
    Page<LoginLog> search(@Param("status") LoginStatus status,
                          @Param("userId") Long userId,
                          @Param("ipAddress") String ipAddress,
                          @Param("from") Instant from,
                          @Param("to") Instant to,
                          Pageable pageable);

    // Admin statisztika
    long countByStatusAndCreatedAtAfter(LoginStatus status, Instant since);
}
