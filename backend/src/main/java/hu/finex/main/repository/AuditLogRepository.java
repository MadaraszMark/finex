package hu.finex.main.repository;

import java.time.Instant;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import hu.finex.main.model.AuditLog;
import hu.finex.main.model.enums.AuditAction;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    // Admin keresés: a művelet és az admin opcionális (null = nincs szűrés), az időszak kötelező
    @Query("select a from AuditLog a where (:action is null or a.action = :action)"
            + " and (:actorId is null or a.actor.id = :actorId)"
            + " and a.createdAt >= :from and a.createdAt < :to"
            + " order by a.createdAt desc, a.id desc")
    Page<AuditLog> search(@Param("action") AuditAction action,
                          @Param("actorId") Long actorId,
                          @Param("from") Instant from,
                          @Param("to") Instant to,
                          Pageable pageable);
}
