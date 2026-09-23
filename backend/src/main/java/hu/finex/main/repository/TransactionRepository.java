package hu.finex.main.repository;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
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
    Page<Transaction> findByAccount_IdAndCreatedAtBetweenOrderByCreatedAtDesc( Long accountId,OffsetDateTime start,OffsetDateTime end,Pageable pageable);

    // Küldött vagy fogadott tranzakciók
    Page<Transaction> findByFromAccountOrToAccountOrderByCreatedAtDesc(String fromAccount,String toAccount,Pageable pageable);

    // Fraud / nagy összegű tranzakciók
    Page<Transaction> findByAccount_IdAndAmountGreaterThan(Long accountId,BigDecimal minAmount,Pageable pageable);

    // Volt-e tranzakció adott idő óta
    boolean existsByAccount_IdAndCreatedAtAfter(Long accountId, OffsetDateTime dateTime);
}
