package hu.finex.main.repository;

import java.time.Instant;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import hu.finex.main.model.SavingsTransaction;
import hu.finex.main.model.enums.SavingsTransactionType;

@Repository
public interface SavingsTransactionRepository extends JpaRepository<SavingsTransaction, Long> {

    // Egy megtakarítás mozgásai, a legújabb elöl
    Page<SavingsTransaction> findBySavingsAccount_IdOrderByCreatedAtDesc(Long savingsAccountId, Pageable pageable);

    // Volt-e már adott típusú tétel egy időpont óta (pl. az adott havi kamat, hogy ne legyen kétszer jóváírva)
    boolean existsBySavingsAccount_IdAndTypeAndCreatedAtGreaterThanEqual(Long savingsAccountId, SavingsTransactionType type, Instant since);
}
