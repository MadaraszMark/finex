package hu.finex.main.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import hu.finex.main.model.SupportTicketMessage;

@Repository
public interface SupportTicketMessageRepository extends JpaRepository<SupportTicketMessage, Long> {

    // Egy ticket üzenetei időrendben
    List<SupportTicketMessage> findByTicket_IdOrderByCreatedAtAsc(Long ticketId);
}
