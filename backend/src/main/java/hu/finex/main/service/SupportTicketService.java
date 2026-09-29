package hu.finex.main.service;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hu.finex.main.dto.CreateSupportTicketRequest;
import hu.finex.main.dto.CreateTicketMessageRequest;
import hu.finex.main.dto.SupportTicketListItemResponse;
import hu.finex.main.dto.SupportTicketResponse;
import hu.finex.main.dto.TicketMessageResponse;
import hu.finex.main.dto.UpdateSupportTicketStatusRequest;
import hu.finex.main.exception.BusinessException;
import hu.finex.main.exception.NotFoundException;
import hu.finex.main.mapper.SupportTicketMapper;
import hu.finex.main.model.SupportTicket;
import hu.finex.main.model.User;
import hu.finex.main.model.enums.NotificationType;
import hu.finex.main.model.enums.TicketStatus;
import hu.finex.main.repository.SupportTicketMessageRepository;
import hu.finex.main.repository.SupportTicketRepository;
import hu.finex.main.security.CurrentUser;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SupportTicketService {

    private static final Map<TicketStatus, String> STATUS_LABELS = Map.of(
            TicketStatus.OPEN, "nyitott",
            TicketStatus.IN_PROGRESS, "folyamatban",
            TicketStatus.RESOLVED, "megoldva");

    private final SupportTicketRepository supportTicketRepository;
    private final SupportTicketMessageRepository supportTicketMessageRepository;
    private final SupportTicketMapper supportTicketMapper;
    private final NotificationService notificationService;
    private final CurrentUser currentUser;

    // Új ticket: egyszerre csak egy nyitott (még meg nem válaszolt) ticketje lehet a felhasználónak
    @Transactional
    public SupportTicketResponse create(CreateSupportTicketRequest request) {
        User user = currentUser.requireEntity();

        if (supportTicketRepository.existsByUser_IdAndStatus(user.getId(), TicketStatus.OPEN)) {
            throw new BusinessException("Már van egy nyitott ticketed, kérjük várd meg a választ.");
        }

        SupportTicket ticket = supportTicketMapper.toEntity(request, user);
        ticket.setStatus(TicketStatus.OPEN);

        ticket = supportTicketRepository.save(ticket);

        return supportTicketMapper.toResponse(ticket);
    }

    // A bejelentkezett felhasználó ticketjei, a legújabb elöl
    @Transactional(readOnly = true)
    public Page<SupportTicketListItemResponse> listMine(Pageable pageable) {
        return supportTicketRepository.findByUser_IdOrderByCreatedAtDesc(currentUser.requireId(), pageable).map(supportTicketMapper::toListItem);
    }

    // Saját ticket a teljes beszélgetéssel
    @Transactional(readOnly = true)
    public SupportTicketResponse getMine(Long id) {
        return toDetails(findOwned(id));
    }

    // Felhasználói válasz a saját ticketjére
    @Transactional
    public SupportTicketResponse addMessage(Long id, CreateTicketMessageRequest request) {
        User user = currentUser.requireEntity();
        SupportTicket ticket = supportTicketRepository.findByIdAndUser_Id(id, user.getId()).orElseThrow(() -> new NotFoundException("Support ticket nem található."));

        if (ticket.getStatus() == TicketStatus.RESOLVED) {
            throw new BusinessException("Lezárt ticketre nem lehet válaszolni, kérjük nyiss újat.");
        }

        supportTicketMessageRepository.save(supportTicketMapper.toMessageEntity(ticket, user, request));
        ticket.setUpdatedAt(Instant.now());

        return toDetails(ticket);
    }

    // Admin: összes ticket, opcionálisan státusz szerint
    @Transactional(readOnly = true)
    public Page<SupportTicketListItemResponse> listAll(TicketStatus status, Pageable pageable) {
        Page<SupportTicket> tickets = status != null
                ? supportTicketRepository.findByStatusOrderByCreatedAtDesc(status, pageable)
                : supportTicketRepository.findAllByOrderByCreatedAtDesc(pageable);

        return tickets.map(supportTicketMapper::toListItem);
    }

    // Admin: bármelyik ticket a teljes beszélgetéssel
    @Transactional(readOnly = true)
    public SupportTicketResponse getById(Long id) {
        return toDetails(findById(id));
    }

    // Admin válasz: a nyitott ticket "folyamatban" állapotba kerül, a felhasználó értesítést kap
    @Transactional
    public SupportTicketResponse reply(Long id, CreateTicketMessageRequest request) {
        User admin = currentUser.requireEntity();
        SupportTicket ticket = findById(id);

        supportTicketMessageRepository.save(supportTicketMapper.toMessageEntity(ticket, admin, request));

        if (ticket.getStatus() == TicketStatus.OPEN) {
            ticket.setStatus(TicketStatus.IN_PROGRESS);
        }
        ticket.setUpdatedAt(Instant.now());

        notificationService.notify(ticket.getUser(), NotificationType.SUPPORT, "Válasz érkezett",
                "Az ügyfélszolgálat válaszolt a „" + ticket.getTitle() + "” ticketedre.");

        return toDetails(ticket);
    }

    // Admin: státusz módosítása, a felhasználó értesítést kap
    @Transactional
    public SupportTicketResponse updateStatus(Long id, UpdateSupportTicketStatusRequest request) {
        SupportTicket ticket = findById(id);

        supportTicketMapper.updateStatus(ticket, request);
        ticket.setUpdatedAt(Instant.now());

        notificationService.notify(ticket.getUser(), NotificationType.SUPPORT, "Ticketed státusza megváltozott",
                "A(z) „" + ticket.getTitle() + "” ticketed új státusza: " + STATUS_LABELS.get(ticket.getStatus()) + ".");

        return toDetails(ticket);
    }

    private SupportTicket findOwned(Long id) {
        return supportTicketRepository.findByIdAndUser_Id(id, currentUser.requireId()).orElseThrow(() -> new NotFoundException("Support ticket nem található."));
    }

    private SupportTicket findById(Long id) {
        return supportTicketRepository.findById(id).orElseThrow(() -> new NotFoundException("Support ticket nem található."));
    }

    private SupportTicketResponse toDetails(SupportTicket ticket) {
        List<TicketMessageResponse> messages = supportTicketMessageRepository.findByTicket_IdOrderByCreatedAtAsc(ticket.getId()).stream()
                .map(supportTicketMapper::toMessageResponse)
                .toList();

        return supportTicketMapper.toResponse(ticket, messages);
    }
}
