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

import hu.finex.main.model.Account;
import hu.finex.main.model.enums.AccountStatus;
import hu.finex.main.model.enums.AccountType;
import jakarta.persistence.LockModeType;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    //Számlaszám alapján lekérés (egyedi)
    Optional<Account> findByAccountNumber(String accountNumber);

    Optional<Account> findFirstByUser_IdAndAccountType(Long userId, AccountType accountType);

    //User összes számlája
    List<Account> findByUser_Id(Long userId);

    //Csak aktív / élő számlák szűrése
    List<Account> findByUser_IdAndStatus(Long userId, AccountStatus status);

    //Státusz ellenőrzés
    boolean existsByAccountNumberAndStatus(String accountNumber, AccountStatus status);

    //Account-number prefix keresés (pl. HUF számlák)
    List<Account> findByAccountNumberStartingWith(String prefix);

    Optional<Account> findFirstByUser_IdAndStatusAndAccountType(Long userId, AccountStatus status, AccountType accountType);

    // A felhasználó számlái nyitás sorrendjében
    List<Account> findByUser_IdOrderByCreatedAtAsc(Long userId);

    // Csak akkor adja vissza a számlát, ha a megadott felhasználóé (jogosultság-ellenőrzés)
    Optional<Account> findByIdAndUser_Id(Long id, Long userId);

    // Tulajdon-ellenőrzés az entitás betöltése nélkül (zárolás előtt ne kerüljön a memóriába)
    boolean existsByIdAndUser_Id(Long id, Long userId);

    // A legrégebbi aktív folyószámla (az "elsődleges" számla)
    Optional<Account> findFirstByUser_IdAndStatusAndAccountTypeOrderByCreatedAtAsc(Long userId, AccountStatus status, AccountType accountType);

    // Csak az azonosító kell, az entitás betöltése nélkül (a zárolás előtt ne kerüljön a memóriába)
    @Query("select a.id from Account a where a.accountNumber = :accountNumber")
    Optional<Long> findIdByAccountNumber(@Param("accountNumber") String accountNumber);

    // Sorzárolás (SELECT ... FOR UPDATE): egyenleg-módosítás előtt, hogy két párhuzamos művelet ne írja felül egymást
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from Account a where a.id = :id")
    Optional<Account> findByIdForUpdate(@Param("id") Long id);

    boolean existsByAccountNumber(String accountNumber);

    // Nem lezárt számlák száma (számlanyitási korlát, utolsó számla lezárásának tiltása)
    long countByUser_IdAndStatusNot(Long userId, AccountStatus status);

    // Admin listázás státusz szerint
    Page<Account> findByStatus(AccountStatus status, Pageable pageable);

    long countByStatusNot(AccountStatus status);

    // A felhasználó összes nem lezárt számlájának egyenlege egy devizában
    @Query("select coalesce(sum(a.balance), 0) from Account a where a.user.id = :userId and a.currency = :currency and a.status <> :excluded")
    BigDecimal sumBalanceByUser(@Param("userId") Long userId, @Param("currency") String currency, @Param("excluded") AccountStatus excluded);

    // A bankban lévő összes betét egy devizában (admin statisztika)
    @Query("select coalesce(sum(a.balance), 0) from Account a where a.currency = :currency and a.status <> :excluded")
    BigDecimal sumBalanceByCurrency(@Param("currency") String currency, @Param("excluded") AccountStatus excluded);
}
