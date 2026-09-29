package hu.finex.main.service;

import hu.finex.main.dto.*;
import hu.finex.main.exception.BusinessException;
import hu.finex.main.exception.NotFoundException;
import hu.finex.main.mapper.SupportTicketMapper;
import hu.finex.main.model.SupportTicket;
import hu.finex.main.model.SupportTicketMessage;
import hu.finex.main.model.User;
import hu.finex.main.model.enums.NotificationType;
import hu.finex.main.model.enums.TicketStatus;
import hu.finex.main.repository.SupportTicketMessageRepository;
import hu.finex.main.repository.SupportTicketRepository;
import hu.finex.main.security.CurrentUser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SupportTicketServiceTest {

    @Mock private SupportTicketRepository supportTicketRepository;
    @Mock private SupportTicketMessageRepository supportTicketMessageRepository;
    @Mock private SupportTicketMapper supportTicketMapper;
    @Mock private NotificationService notificationService;
    @Mock private CurrentUser currentUser;

    @InjectMocks private SupportTicketService service;

    @Test
    void create_shouldThrowBusinessException_whenOpenTicketExists() {
        User user = User.builder().id(10L).build();
        when(currentUser.requireEntity()).thenReturn(user);
        when(supportTicketRepository.existsByUser_IdAndStatus(10L, TicketStatus.OPEN)).thenReturn(true);

        CreateSupportTicketRequest req = CreateSupportTicketRequest.builder()
                .title("T")
                .message("M")
                .build();

        assertThrows(BusinessException.class, () -> service.create(req));

        verify(supportTicketRepository, never()).save(any());
        verifyNoInteractions(supportTicketMapper);
    }

    @Test
    void create_shouldSetStatusOpen_saveAndReturnResponse() {
        User user = User.builder().id(10L).build();
        when(currentUser.requireEntity()).thenReturn(user);
        when(supportTicketRepository.existsByUser_IdAndStatus(10L, TicketStatus.OPEN)).thenReturn(false);

        CreateSupportTicketRequest req = CreateSupportTicketRequest.builder()
                .title("Kártya tiltás")
                .message("Elvesztettem a kártyámat")
                .build();

        SupportTicket mapped = SupportTicket.builder().user(user).title("Kártya tiltás").message("Elvesztettem a kártyámat").build();
        when(supportTicketMapper.toEntity(req, user)).thenReturn(mapped);

        SupportTicket saved = SupportTicket.builder().id(100L).user(user).status(TicketStatus.OPEN).build();
        when(supportTicketRepository.save(mapped)).thenReturn(saved);
        when(supportTicketMapper.toResponse(saved)).thenReturn(SupportTicketResponse.builder().id(100L).status(TicketStatus.OPEN).build());

        SupportTicketResponse resp = service.create(req);

        assertEquals(100L, resp.getId());
        assertEquals(TicketStatus.OPEN, mapped.getStatus());
    }

    @Test
    void listMine_shouldReturnOwnTickets() {
        when(currentUser.requireId()).thenReturn(10L);

        Pageable pageable = PageRequest.of(0, 10);
        SupportTicket ticket = SupportTicket.builder().id(1L).build();
        when(supportTicketRepository.findByUser_IdOrderByCreatedAtDesc(10L, pageable)).thenReturn(new PageImpl<>(List.of(ticket), pageable, 1));
        when(supportTicketMapper.toListItem(ticket)).thenReturn(SupportTicketListItemResponse.builder().id(1L).build());

        Page<SupportTicketListItemResponse> page = service.listMine(pageable);

        assertEquals(1, page.getTotalElements());
        assertEquals(1L, page.getContent().get(0).getId());
    }

    @Test
    void getMine_shouldReturnTicketWithConversation() {
        when(currentUser.requireId()).thenReturn(10L);

        SupportTicket ticket = SupportTicket.builder().id(1L).build();
        when(supportTicketRepository.findByIdAndUser_Id(1L, 10L)).thenReturn(Optional.of(ticket));

        SupportTicketMessage message = SupportTicketMessage.builder().id(5L).build();
        when(supportTicketMessageRepository.findByTicket_IdOrderByCreatedAtAsc(1L)).thenReturn(List.of(message));

        TicketMessageResponse messageResp = TicketMessageResponse.builder().id(5L).build();
        when(supportTicketMapper.toMessageResponse(message)).thenReturn(messageResp);
        when(supportTicketMapper.toResponse(ticket, List.of(messageResp))).thenReturn(SupportTicketResponse.builder().id(1L).messages(List.of(messageResp)).build());

        SupportTicketResponse resp = service.getMine(1L);

        assertEquals(1, resp.getMessages().size());
    }

    @Test
    void getMine_shouldThrowNotFound_whenTicketBelongsToOtherUser() {
        when(currentUser.requireId()).thenReturn(10L);
        when(supportTicketRepository.findByIdAndUser_Id(1L, 10L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.getMine(1L));
    }

    @Test
    void addMessage_shouldThrowBusinessException_whenTicketIsResolved() {
        User user = User.builder().id(10L).build();
        when(currentUser.requireEntity()).thenReturn(user);
        when(supportTicketRepository.findByIdAndUser_Id(1L, 10L)).thenReturn(Optional.of(SupportTicket.builder().id(1L).status(TicketStatus.RESOLVED).build()));

        CreateTicketMessageRequest req = CreateTicketMessageRequest.builder().message("Még egy kérdés").build();

        assertThrows(BusinessException.class, () -> service.addMessage(1L, req));

        verifyNoInteractions(supportTicketMessageRepository);
    }

    @Test
    void addMessage_shouldSaveUsersMessage() {
        User user = User.builder().id(10L).build();
        when(currentUser.requireEntity()).thenReturn(user);

        SupportTicket ticket = SupportTicket.builder().id(1L).status(TicketStatus.IN_PROGRESS).build();
        when(supportTicketRepository.findByIdAndUser_Id(1L, 10L)).thenReturn(Optional.of(ticket));

        CreateTicketMessageRequest req = CreateTicketMessageRequest.builder().message("Köszönöm!").build();
        SupportTicketMessage entity = SupportTicketMessage.builder().message("Köszönöm!").build();
        when(supportTicketMapper.toMessageEntity(ticket, user, req)).thenReturn(entity);
        when(supportTicketMessageRepository.findByTicket_IdOrderByCreatedAtAsc(1L)).thenReturn(List.of());
        when(supportTicketMapper.toResponse(ticket, List.of())).thenReturn(SupportTicketResponse.builder().id(1L).build());

        service.addMessage(1L, req);

        verify(supportTicketMessageRepository).save(entity);
        assertNotNull(ticket.getUpdatedAt());
        assertEquals(TicketStatus.IN_PROGRESS, ticket.getStatus());
    }

    @Test
    void listAll_shouldFilterByStatus_whenGiven() {
        Pageable pageable = PageRequest.of(0, 10);
        SupportTicket ticket = SupportTicket.builder().id(1L).build();
        when(supportTicketRepository.findByStatusOrderByCreatedAtDesc(TicketStatus.OPEN, pageable)).thenReturn(new PageImpl<>(List.of(ticket), pageable, 1));
        when(supportTicketMapper.toListItem(ticket)).thenReturn(SupportTicketListItemResponse.builder().id(1L).build());

        Page<SupportTicketListItemResponse> page = service.listAll(TicketStatus.OPEN, pageable);

        assertEquals(1, page.getTotalElements());
        verify(supportTicketRepository, never()).findAllByOrderByCreatedAtDesc(any());
    }

    @Test
    void listAll_shouldListEverything_whenNoStatus() {
        Pageable pageable = PageRequest.of(0, 10);
        when(supportTicketRepository.findAllByOrderByCreatedAtDesc(pageable)).thenReturn(Page.empty(pageable));

        service.listAll(null, pageable);

        verify(supportTicketRepository, never()).findByStatusOrderByCreatedAtDesc(any(), any());
    }

    @Test
    void reply_shouldMoveOpenTicketToInProgress_andNotifyUser() {
        User admin = User.builder().id(1L).build();
        User owner = User.builder().id(10L).build();
        when(currentUser.requireEntity()).thenReturn(admin);

        SupportTicket ticket = SupportTicket.builder().id(1L).user(owner).title("Kártya tiltás").status(TicketStatus.OPEN).build();
        when(supportTicketRepository.findById(1L)).thenReturn(Optional.of(ticket));

        CreateTicketMessageRequest req = CreateTicketMessageRequest.builder().message("A kártyát letiltottuk.").build();
        SupportTicketMessage entity = SupportTicketMessage.builder().message("A kártyát letiltottuk.").build();
        when(supportTicketMapper.toMessageEntity(ticket, admin, req)).thenReturn(entity);
        when(supportTicketMessageRepository.findByTicket_IdOrderByCreatedAtAsc(1L)).thenReturn(List.of());
        when(supportTicketMapper.toResponse(ticket, List.of())).thenReturn(SupportTicketResponse.builder().id(1L).status(TicketStatus.IN_PROGRESS).build());

        SupportTicketResponse resp = service.reply(1L, req);

        assertEquals(TicketStatus.IN_PROGRESS, ticket.getStatus());
        assertEquals(TicketStatus.IN_PROGRESS, resp.getStatus());
        verify(supportTicketMessageRepository).save(entity);
        verify(notificationService).notify(eq(owner), eq(NotificationType.SUPPORT), eq("Válasz érkezett"), contains("Kártya tiltás"));
    }

    @Test
    void reply_shouldThrowNotFound_whenTicketMissing() {
        when(currentUser.requireEntity()).thenReturn(User.builder().id(1L).build());
        when(supportTicketRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.reply(1L, CreateTicketMessageRequest.builder().message("x").build()));

        verifyNoInteractions(supportTicketMessageRepository, notificationService);
    }

    @Test
    void updateStatus_shouldUpdate_andNotifyUser() {
        User owner = User.builder().id(10L).build();
        SupportTicket ticket = SupportTicket.builder().id(1L).user(owner).title("Kártya tiltás").status(TicketStatus.IN_PROGRESS).build();
        when(supportTicketRepository.findById(1L)).thenReturn(Optional.of(ticket));

        UpdateSupportTicketStatusRequest req = UpdateSupportTicketStatusRequest.builder().status(TicketStatus.RESOLVED).build();
        doAnswer(invocation -> {
            ticket.setStatus(TicketStatus.RESOLVED);
            return null;
        }).when(supportTicketMapper).updateStatus(ticket, req);

        when(supportTicketMessageRepository.findByTicket_IdOrderByCreatedAtAsc(1L)).thenReturn(List.of());
        when(supportTicketMapper.toResponse(ticket, List.of())).thenReturn(SupportTicketResponse.builder().id(1L).status(TicketStatus.RESOLVED).build());

        SupportTicketResponse resp = service.updateStatus(1L, req);

        assertEquals(TicketStatus.RESOLVED, resp.getStatus());
        verify(notificationService).notify(eq(owner), eq(NotificationType.SUPPORT), anyString(), contains("megoldva"));
    }

    @Test
    void updateStatus_shouldThrowNotFound_whenTicketMissing() {
        when(supportTicketRepository.findById(1L)).thenReturn(Optional.empty());

        UpdateSupportTicketStatusRequest req = UpdateSupportTicketStatusRequest.builder().status(TicketStatus.RESOLVED).build();

        assertThrows(NotFoundException.class, () -> service.updateStatus(1L, req));

        verifyNoInteractions(supportTicketMapper, notificationService);
    }
}
