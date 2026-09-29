package hu.finex.main.repository;

import java.time.Instant;
import java.util.List;

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
    Page<BalanceHistory> findByAccount_IdAndCreatedAtBetweenOrderByCreatedAtAsc(Long accountId,Instant start,Instant end,Pageable pageable);

    // Grafikonhoz: az időszak összes pontja lapozás nélkül
    List<BalanceHistory> findByAccount_IdAndCreatedAtBetweenOrderByCreatedAtAsc(Long accountId, Instant start, Instant end);

    boolean existsByAccount_IdAndCreatedAtAfter(Long accountId, Instant time);
}
