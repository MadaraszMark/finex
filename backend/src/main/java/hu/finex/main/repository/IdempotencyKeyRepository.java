package hu.finex.main.repository;

import java.time.Instant;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import hu.finex.main.model.IdempotencyKey;

public interface IdempotencyKeyRepository extends JpaRepository<IdempotencyKey, Long> {

    // 1, ha most foglaltuk le a kulcsot, 0, ha már létezett (párhuzamosan csak egy kérés foglalhatja le)
    @Modifying
    @Query(value = "insert into idempotency_keys (user_id, idempotency_key, request_hash) values (:userId, :key, :hash)"
            + " on conflict (user_id, idempotency_key) do nothing", nativeQuery = true)
    int insertIfAbsent(@Param("userId") Long userId, @Param("key") String key, @Param("hash") String hash);

    Optional<IdempotencyKey> findByUserIdAndIdempotencyKey(Long userId, String idempotencyKey);

    // A régi kulcsokat nem kell örökké őrizni (az ütemező törli)
    @Modifying
    @Query("delete from IdempotencyKey k where k.createdAt < :before")
    int deleteCreatedBefore(@Param("before") Instant before);
}
