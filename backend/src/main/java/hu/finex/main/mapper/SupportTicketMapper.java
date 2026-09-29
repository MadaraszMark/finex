package hu.finex.main.mapper;

import java.util.List;

import org.springframework.stereotype.Component;

import hu.finex.main.dto.CreateSupportTicketRequest;
import hu.finex.main.dto.CreateTicketMessageRequest;
import hu.finex.main.dto.SupportTicketListItemResponse;
import hu.finex.main.dto.SupportTicketResponse;
import hu.finex.main.dto.TicketMessageResponse;
import hu.finex.main.dto.UpdateSupportTicketStatusRequest;
import hu.finex.main.model.SupportTicket;
import hu.finex.main.model.SupportTicketMessage;
import hu.finex.main.model.User;
import hu.finex.main.model.enums.UserRole;

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
        return toResponse(ticket, List.of());
    }

    public SupportTicketResponse toResponse(SupportTicket ticket, List<TicketMessageResponse> messages) {
        return SupportTicketResponse.builder()
                .id(ticket.getId())
                .userId(ticket.getUser().getId())
                .userFullName(ticket.getUser().getFullName())
                .userEmail(ticket.getUser().getEmail())
                .title(ticket.getTitle())
                .message(ticket.getMessage())
                .status(ticket.getStatus())
                .messages(messages)
                .createdAt(ticket.getCreatedAt())
                .updatedAt(ticket.getUpdatedAt())
                .build();
    }

    public SupportTicketListItemResponse toListItem(SupportTicket ticket) {
        return SupportTicketListItemResponse.builder()
                .id(ticket.getId())
                .title(ticket.getTitle())
                .userFullName(ticket.getUser().getFullName())
                .status(ticket.getStatus())
                .createdAt(ticket.getCreatedAt())
                .updatedAt(ticket.getUpdatedAt())
                .build();
    }

    public SupportTicketMessage toMessageEntity(SupportTicket ticket, User author, CreateTicketMessageRequest request) {
        return SupportTicketMessage.builder()
                .ticket(ticket)
                .author(author)
                .message(request.getMessage())
                .build();
    }

    public TicketMessageResponse toMessageResponse(SupportTicketMessage message) {
        return TicketMessageResponse.builder()
                .id(message.getId())
                .authorName(message.getAuthor().getFullName())
                .staff(message.getAuthor().getRole() == UserRole.ADMIN)
                .message(message.getMessage())
                .createdAt(message.getCreatedAt())
                .build();
    }
}

