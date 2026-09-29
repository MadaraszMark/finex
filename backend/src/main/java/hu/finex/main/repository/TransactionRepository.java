package hu.finex.main.repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collection;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import hu.finex.main.model.Transaction;
import hu.finex.main.model.enums.TransactionType;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    // Account összes tranzakciója
    Page<Transaction> findByAccount_IdOrderByCreatedAtDesc(Long accountId, Pageable pageable);

    // Csak adott tranzakciótípus
    Page<Transaction> findByAccount_IdAndTypeOrderByCreatedAtDesc(Long accountId,TransactionType type,Pageable pageable);

    // Időintervallum alapján
    Page<Transaction> findByAccount_IdAndCreatedAtBetweenOrderByCreatedAtDesc( Long accountId,Instant start,Instant end,Pageable pageable);

    // Küldött vagy fogadott tranzakciók
    Page<Transaction> findByFromAccountOrToAccountOrderByCreatedAtDesc(String fromAccount,String toAccount,Pageable pageable);

    // Fraud / nagy összegű tranzakciók
    Page<Transaction> findByAccount_IdAndAmountGreaterThan(Long accountId,BigDecimal minAmount,Pageable pageable);

    // Volt-e tranzakció adott idő óta
    boolean existsByAccount_IdAndCreatedAtAfter(Long accountId, Instant dateTime);

    // Csak akkor adja vissza a tételt, ha a felhasználó számláján van (jogosultság-ellenőrzés)
    Optional<Transaction> findByIdAndAccount_User_Id(Long id, Long userId);

    boolean existsByIdAndAccount_User_Id(Long id, Long userId);

    // A felhasználó tételeinek keresése: a szűrők opcionálisak (null = nincs szűrés), az időszaknak viszont mindig van határa
    // (lásd DateUtils.BEGINNING_OF_TIME / END_OF_TIME), a search kisbetűs és %-jelekkel érkezik
    @Query("select t from Transaction t where t.account.user.id = :userId"
            + " and (:accountId is null or t.account.id = :accountId)"
            + " and (:type is null or t.type = :type)"
            + " and t.createdAt >= :from and t.createdAt < :to"
            + " and (:minAmount is null or t.amount >= :minAmount)"
            + " and (:maxAmount is null or t.amount <= :maxAmount)"
            + " and (:search is null or lower(t.message) like :search or lower(t.partnerName) like :search)"
            + " and (:categoryId is null or exists (select tc.id from TransactionCategory tc where tc.transaction = t and tc.category.id = :categoryId))")
    Page<Transaction> search(@Param("userId") Long userId,
                             @Param("accountId") Long accountId,
                             @Param("type") TransactionType type,
                             @Param("from") Instant from,
                             @Param("to") Instant to,
                             @Param("minAmount") BigDecimal minAmount,
                             @Param("maxAmount") BigDecimal maxAmount,
                             @Param("search") String search,
                             @Param("categoryId") Long categoryId,
                             Pageable pageable);

    // Adott típusú tételek összege egy időpont óta (pl. a mai kimenő utalások a napi limithez)
    @Query("select coalesce(sum(t.amount), 0) from Transaction t where t.account.id = :accountId and t.type = :type and t.createdAt >= :since")
    BigDecimal sumAmountByAccountAndTypeSince(@Param("accountId") Long accountId, @Param("type") TransactionType type, @Param("since") Instant since);

    // Egy kártya költése egy időpont óta (a kártya napi limitjéhez)
    @Query("select coalesce(sum(t.amount), 0) from Transaction t where t.card.id = :cardId and t.createdAt >= :since")
    BigDecimal sumCardSpendingSince(@Param("cardId") Long cardId, @Param("since") Instant since);

    // A felhasználó bevételei vagy kiadásai egy devizában egy időpont óta (dashboard)
    @Query("select coalesce(sum(t.amount), 0) from Transaction t where t.account.user.id = :userId and t.currency = :currency and t.type in :types and t.createdAt >= :since")
    BigDecimal sumByUserAndTypesSince(@Param("userId") Long userId, @Param("currency") String currency, @Param("types") Collection<TransactionType> types, @Param("since") Instant since);

    // Admin statisztika: tételek száma és forgalma egy időpont óta
    long countByCreatedAtGreaterThanEqual(Instant since);

    @Query("select coalesce(sum(t.amount), 0) from Transaction t where t.currency = :currency and t.type in :types and t.createdAt >= :since")
    BigDecimal sumVolumeSince(@Param("currency") String currency, @Param("types") Collection<TransactionType> types, @Param("since") Instant since);
}
