package hu.finex.main.repository;

import java.math.BigDecimal;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import hu.finex.main.model.SavingsAccount;
import hu.finex.main.model.enums.SavingsStatus;

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
}

