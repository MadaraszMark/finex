package hu.finex.main.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import hu.finex.main.model.User;
import hu.finex.main.model.enums.UserStatus;
import jakarta.persistence.LockModeType;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmailIgnoreCase(String email);
    Optional<User> findByPhone(String phone);
    boolean existsByEmailIgnoreCase(String email);

    // Admin keresés névre vagy e-mail címre (a search már kisbetűs, %-jelekkel)
    @Query("select u from User u where :search is null or lower(u.email) like :search or lower(u.firstName) like :search or lower(u.lastName) like :search")
    Page<User> search(@Param("search") String search, Pageable pageable);

    // Admin statisztika
    long countByStatus(UserStatus status);

    // Sorzárolás, hogy a felhasználónkénti korlátok (pl. max. 5 számla) párhuzamosan se sérüljenek
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from User u where u.id = :id")
    Optional<User> findByIdForUpdate(@Param("id") Long id);
}
