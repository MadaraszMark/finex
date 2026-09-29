package hu.finex.main.repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import hu.finex.main.dto.CategorySpendingResponse;
import hu.finex.main.model.TransactionCategory;
import hu.finex.main.model.enums.TransactionType;

@Repository
public interface TransactionCategoryRepository extends JpaRepository<TransactionCategory, Long> {

    // Egy tranzakcióhoz tartozó kategóriák lista
    List<TransactionCategory> findByTransaction_Id(Long transactionId);

    // Egy kategóriához tartozó tranzakciók lista
    List<TransactionCategory> findByCategory_Id(Long categoryId);

    // Létezik-e már ez a kapcsolat
    boolean existsByTransaction_IdAndCategory_Id(Long transactionId, Long categoryId);

    // Egy tranzakció összes kategóriájának törlése (pl. update esetén)
    void deleteByTransaction_Id(Long transactionId);

    // Egy lapnyi tranzakció kategóriái egyetlen lekérdezéssel (N+1 lekérdezés helyett)
    List<TransactionCategory> findByTransaction_IdIn(Collection<Long> transactionIds);

    // Használja-e tranzakció a kategóriát (törlés előtti ellenőrzés)
    boolean existsByCategory_Id(Long categoryId);

    // Csak akkor adja vissza a kapcsolatot, ha a tranzakció a felhasználóé
    Optional<TransactionCategory> findByIdAndTransaction_Account_User_Id(Long id, Long userId);

    // Költések kategóriánként egy időszakban, a legnagyobb költés elöl (kategória-diagramhoz)
    @Query("select new hu.finex.main.dto.CategorySpendingResponse(c.id, c.name, c.icon, sum(t.amount), count(t))"
            + " from TransactionCategory tc join tc.transaction t join tc.category c"
            + " where t.account.user.id = :userId and t.currency = :currency and t.type in :types"
            + " and t.createdAt >= :from and t.createdAt < :to"
            + " group by c.id, c.name, c.icon"
            + " order by sum(t.amount) desc")
    List<CategorySpendingResponse> sumSpendingByCategory(@Param("userId") Long userId,
                                                         @Param("currency") String currency,
                                                         @Param("types") Collection<TransactionType> types,
                                                         @Param("from") Instant from,
                                                         @Param("to") Instant to);
}
