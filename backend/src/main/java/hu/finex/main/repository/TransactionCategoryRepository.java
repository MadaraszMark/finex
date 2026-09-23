package hu.finex.main.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import hu.finex.main.model.TransactionCategory;

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
}
