package hu.finex.main.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import hu.finex.main.model.SupportTicket;
import hu.finex.main.model.enums.TicketStatus;

@Repository
public interface SupportTicketRepository extends JpaRepository<SupportTicket, Long> {

    // Egy user összes ticketje
    Page<SupportTicket> findByUser_IdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    // Státusz alapján listázás
    Page<SupportTicket> findByStatusOrderByCreatedAtDesc(TicketStatus status, Pageable pageable);

    // User + státusz kombináció
    Page<SupportTicket> findByUser_IdAndStatusOrderByCreatedAtDesc(Long userId,TicketStatus status,Pageable pageable);

    // Létezik-e ticket adott userhez és adott státusszal? (pl. ne nyisson új duplikáltat)
    boolean existsByUser_IdAndStatus(Long userId, TicketStatus status);
}
