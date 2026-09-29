package hu.finex.main.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import hu.finex.main.model.SavingsAccount;
import hu.finex.main.model.enums.SavingsStatus;
import jakarta.persistence.LockModeType;

@Repository
public interface SavingsAccountRepository extends JpaRepository<SavingsAccount, Long> {

    // Egy user összes megtakarítása
    Page<SavingsAccount> findByUser_IdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    // Meghatározott státusz szerinti listázás
    Page<SavingsAccount> findByUser_IdAndStatusOrderByCreatedAtDesc(Long userId,SavingsStatus status,Pageable pageable);

    // Létezik-e egy adott nevű megtakarítás
    boolean existsByUser_IdAndName(Long userId, String name);

    // Minimális egyenleg feletti megtakarítások (portfólió elemzéshez)
    Page<SavingsAccount> findByUser_IdAndBalanceGreaterThanEqual(Long userId,BigDecimal minBalance,Pageable pageable);

    // Státusz alapján létezik-e aktív megtakarítás
    boolean existsByUser_IdAndStatus(Long userId, SavingsStatus status);

    // A felhasználó nem lezárt megtakarításai (a lezártak csak az előzményekben számítanak)
    List<SavingsAccount> findByUser_IdAndStatusNotOrderByCreatedAtAsc(Long userId, SavingsStatus status);

    // Csak akkor adja vissza a megtakarítást, ha a megadott felhasználóé (jogosultság-ellenőrzés)
    Optional<SavingsAccount> findByIdAndUser_Id(Long id, Long userId);

    boolean existsByIdAndUser_Id(Long id, Long userId);

    // Névütközés csak a nem lezárt megtakarítások között számít (ugyanez az adatbázis részleges egyedi indexe)
    boolean existsByUser_IdAndNameIgnoreCaseAndStatusNot(Long userId, String name, SavingsStatus status);

    // Sorzárolás egyenleg-módosítás előtt
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from SavingsAccount s where s.id = :id")
    Optional<SavingsAccount> findByIdForUpdate(@Param("id") Long id);

    // A havi kamatjóváírásban részt vevő megtakarítások
    @Query("select s.id from SavingsAccount s where s.status = :status and s.balance > 0 and s.interestRate > 0")
    List<Long> findIdsForInterest(@Param("status") SavingsStatus status);

    @Query("select coalesce(sum(s.balance), 0) from SavingsAccount s where s.user.id = :userId and s.currency = :currency and s.status <> :excluded")
    BigDecimal sumBalanceByUser(@Param("userId") Long userId, @Param("currency") String currency, @Param("excluded") SavingsStatus excluded);
}
