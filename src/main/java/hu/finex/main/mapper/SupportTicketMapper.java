package hu.finex.main.mapper;

import org.springframework.stereotype.Component;

import hu.finex.main.dto.CreateSupportTicketRequest;
import hu.finex.main.dto.SupportTicketResponse;
import hu.finex.main.dto.UpdateSupportTicketStatusRequest;
import hu.finex.main.model.SupportTicket;
import hu.finex.main.model.User;

@Component
public class SupportTicketMapper {

    public SupportTicket toEntity(CreateSupportTicketRequest request, User user) {
        return SupportTicket.builder()
                .user(user)
                .title(request.getTitle())
                .message(request.getMessage())
                .status(null)
                .build();
    }

    public void updateStatus(SupportTicket ticket, UpdateSupportTicketStatusRequest request) {
        ticket.setStatus(request.getStatus());
    }

    public SupportTicketResponse toResponse(SupportTicket ticket) {
        return SupportTicketResponse.builder()
                .id(ticket.getId())
                .userId(ticket.getUser().getId())
                .title(ticket.getTitle())
                .message(ticket.getMessage())
                .status(ticket.getStatus())
                .createdAt(ticket.getCreatedAt())
                .updatedAt(ticket.getUpdatedAt())
                .build();
    }
}

