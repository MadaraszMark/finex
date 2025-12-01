package hu.finex.main.repository;

import java.time.OffsetDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import hu.finex.main.model.BalanceHistory;

@Repository
public interface BalanceHistoryRepository extends JpaRepository<BalanceHistory, Long> {

    // Egy account összes balance history-ja (idő szerint)
    Page<BalanceHistory> findByAccount_IdOrderByCreatedAtAsc(Long accountId, Pageable pageable);

    // Időintervallumra szűrés (grafikonokhoz, dashboardhoz)
    Page<BalanceHistory> findByAccount_IdAndCreatedAtBetweenOrderByCreatedAtAsc(Long accountId,OffsetDateTime start,OffsetDateTime end,Pageable pageable);

    boolean existsByAccount_IdAndCreatedAtAfter(Long accountId, OffsetDateTime time);
}
