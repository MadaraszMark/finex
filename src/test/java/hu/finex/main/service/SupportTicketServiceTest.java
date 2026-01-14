package hu.finex.main.service;

import hu.finex.main.dto.CreateSupportTicketRequest;
import hu.finex.main.dto.SupportTicketResponse;
import hu.finex.main.dto.UpdateSupportTicketStatusRequest;
import hu.finex.main.exception.BusinessException;
import hu.finex.main.exception.NotFoundException;
import hu.finex.main.mapper.SupportTicketMapper;
import hu.finex.main.model.SupportTicket;
import hu.finex.main.model.User;
import hu.finex.main.model.enums.TicketStatus;
import hu.finex.main.repository.SupportTicketRepository;
import hu.finex.main.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SupportTicketServiceTest {

    @Mock private SupportTicketRepository supportTicketRepository;
    @Mock private UserRepository userRepository;
    @Mock private SupportTicketMapper supportTicketMapper;

    @InjectMocks private SupportTicketService service;

    @Test
    void create_shouldThrowNotFound_whenUserMissing() {
        Principal principal = () -> "missing@example.com";
        CreateSupportTicketRequest req = CreateSupportTicketRequest.builder()
                .title("T")
                .message("M")
                .build();

        when(userRepository.findByEmailIgnoreCase("missing@example.com")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.create(req, principal));

        verify(userRepository).findByEmailIgnoreCase("missing@example.com");
        verifyNoInteractions(supportTicketRepository, supportTicketMapper);
    }

    @Test
    void create_shouldThrowBusinessException_whenOpenTicketExists() {
        Principal principal = () -> "user@example.com";
        CreateSupportTicketRequest req = CreateSupportTicketRequest.builder()
                .title("T")
                .message("M")
                .build();

        User user = User.builder().id(10L).email("user@example.com").build();
        when(userRepository.findByEmailIgnoreCase("user@example.com")).thenReturn(Optional.of(user));
        when(supportTicketRepository.existsByUser_IdAndStatus(10L, TicketStatus.OPEN)).thenReturn(true);

        assertThrows(BusinessException.class, () -> service.create(req, principal));

        verify(userRepository).findByEmailIgnoreCase("user@example.com");
        verify(supportTicketRepository).existsByUser_IdAndStatus(10L, TicketStatus.OPEN);
        verifyNoInteractions(supportTicketMapper);
        verify(supportTicketRepository, never()).save(any());
    }

    @Test
    void create_shouldSetStatusOpen_saveAndReturnResponse() {
        Principal principal = () -> "user@example.com";
        CreateSupportTicketRequest req = CreateSupportTicketRequest.builder()
                .title("Title")
                .message("Message")
                .build();

        User user = User.builder().id(10L).email("user@example.com").build();
        when(userRepository.findByEmailIgnoreCase("user@example.com")).thenReturn(Optional.of(user));
        when(supportTicketRepository.existsByUser_IdAndStatus(10L, TicketStatus.OPEN)).thenReturn(false);

        SupportTicket mapped = SupportTicket.builder()
                .user(user)
                .title("Title")
                .message("Message")
                .status(null)
                .build();
        when(supportTicketMapper.toEntity(req, user)).thenReturn(mapped);

        SupportTicket saved = SupportTicket.builder()
                .id(99L)
                .user(user)
                .title("Title")
                .message("Message")
                .status(TicketStatus.OPEN)
                .build();
        when(supportTicketRepository.save(any(SupportTicket.class))).thenReturn(saved);

        SupportTicketResponse expected = SupportTicketResponse.builder()
                .id(99L)
                .userId(10L)
                .title("Title")
                .message("Message")
                .status(TicketStatus.OPEN)
                .build();
        when(supportTicketMapper.toResponse(saved)).thenReturn(expected);

        SupportTicketResponse resp = service.create(req, principal);

        assertNotNull(resp);
        assertEquals(99L, resp.getId());
        assertEquals(10L, resp.getUserId());
        assertEquals(TicketStatus.OPEN, resp.getStatus());

        ArgumentCaptor<SupportTicket> ticketCaptor = ArgumentCaptor.forClass(SupportTicket.class);
        verify(supportTicketRepository).save(ticketCaptor.capture());
        assertEquals(TicketStatus.OPEN, ticketCaptor.getValue().getStatus());

        verify(userRepository).findByEmailIgnoreCase("user@example.com");
        verify(supportTicketRepository).existsByUser_IdAndStatus(10L, TicketStatus.OPEN);
        verify(supportTicketMapper).toEntity(req, user);
        verify(supportTicketMapper).toResponse(saved);
    }

    @Test
    void getById_shouldReturnResponse() {
        SupportTicket ticket = SupportTicket.builder().id(5L).build();
        when(supportTicketRepository.findById(5L)).thenReturn(Optional.of(ticket));

        SupportTicketResponse expected = SupportTicketResponse.builder().id(5L).build();
        when(supportTicketMapper.toResponse(ticket)).thenReturn(expected);

        SupportTicketResponse resp = service.getById(5L);

        assertNotNull(resp);
        assertEquals(5L, resp.getId());

        verify(supportTicketRepository).findById(5L);
        verify(supportTicketMapper).toResponse(ticket);
    }

    @Test
    void getById_shouldThrowNotFound_whenMissing() {
        when(supportTicketRepository.findById(5L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.getById(5L));

        verify(supportTicketRepository).findById(5L);
        verifyNoInteractions(supportTicketMapper);
    }

    @Test
    void listByUser_shouldThrowNotFound_whenUserMissing() {
        Pageable pageable = PageRequest.of(0, 10);
        when(userRepository.findById(7L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.listByUser(7L, pageable));

        verify(userRepository).findById(7L);
        verifyNoInteractions(supportTicketRepository, supportTicketMapper);
    }

    @Test
    void listByUser_shouldReturnMappedPage() {
        Pageable pageable = PageRequest.of(0, 2);
        when(userRepository.findById(7L)).thenReturn(Optional.of(User.builder().id(7L).build()));

        SupportTicket t1 = SupportTicket.builder().id(1L).build();
        SupportTicket t2 = SupportTicket.builder().id(2L).build();
        Page<SupportTicket> page = new PageImpl<>(List.of(t1, t2), pageable, 2);

        when(supportTicketRepository.findByUser_IdOrderByCreatedAtDesc(7L, pageable)).thenReturn(page);

        SupportTicketResponse r1 = SupportTicketResponse.builder().id(1L).build();
        SupportTicketResponse r2 = SupportTicketResponse.builder().id(2L).build();
        when(supportTicketMapper.toResponse(t1)).thenReturn(r1);
        when(supportTicketMapper.toResponse(t2)).thenReturn(r2);

        Page<SupportTicketResponse> resp = service.listByUser(7L, pageable);

        assertEquals(2, resp.getTotalElements());
        assertSame(r1, resp.getContent().get(0));
        assertSame(r2, resp.getContent().get(1));

        verify(supportTicketRepository).findByUser_IdOrderByCreatedAtDesc(7L, pageable);
        verify(supportTicketMapper).toResponse(t1);
        verify(supportTicketMapper).toResponse(t2);
    }

    @Test
    void listByStatus_shouldReturnMappedPage() {
        Pageable pageable = PageRequest.of(0, 1);

        SupportTicket ticket = SupportTicket.builder().id(1L).status(TicketStatus.OPEN).build();
        Page<SupportTicket> page = new PageImpl<>(List.of(ticket), pageable, 1);

        when(supportTicketRepository.findByStatusOrderByCreatedAtDesc(TicketStatus.OPEN, pageable)).thenReturn(page);

        SupportTicketResponse item = SupportTicketResponse.builder().id(1L).build();
        when(supportTicketMapper.toResponse(ticket)).thenReturn(item);

        Page<SupportTicketResponse> resp = service.listByStatus(TicketStatus.OPEN, pageable);

        assertEquals(1, resp.getTotalElements());
        assertSame(item, resp.getContent().get(0));

        verify(supportTicketRepository).findByStatusOrderByCreatedAtDesc(TicketStatus.OPEN, pageable);
        verify(supportTicketMapper).toResponse(ticket);
    }

    @Test
    void listByUserAndStatus_shouldThrowNotFound_whenUserMissing() {
        Pageable pageable = PageRequest.of(0, 10);
        when(userRepository.findById(7L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.listByUserAndStatus(7L, TicketStatus.OPEN, pageable));

        verify(userRepository).findById(7L);
        verifyNoInteractions(supportTicketRepository, supportTicketMapper);
    }

    @Test
    void listByUserAndStatus_shouldReturnMappedPage() {
        Pageable pageable = PageRequest.of(0, 1);
        when(userRepository.findById(7L)).thenReturn(Optional.of(User.builder().id(7L).build()));

        SupportTicket ticket = SupportTicket.builder().id(1L).status(TicketStatus.OPEN).build();
        Page<SupportTicket> page = new PageImpl<>(List.of(ticket), pageable, 1);

        when(supportTicketRepository.findByUser_IdAndStatusOrderByCreatedAtDesc(7L, TicketStatus.OPEN, pageable))
                .thenReturn(page);

        SupportTicketResponse item = SupportTicketResponse.builder().id(1L).build();
        when(supportTicketMapper.toResponse(ticket)).thenReturn(item);

        Page<SupportTicketResponse> resp = service.listByUserAndStatus(7L, TicketStatus.OPEN, pageable);

        assertEquals(1, resp.getTotalElements());
        assertSame(item, resp.getContent().get(0));

        verify(supportTicketRepository).findByUser_IdAndStatusOrderByCreatedAtDesc(7L, TicketStatus.OPEN, pageable);
        verify(supportTicketMapper).toResponse(ticket);
    }

    @Test
    void updateStatus_shouldThrowNotFound_whenTicketMissing() {
        when(supportTicketRepository.findById(5L)).thenReturn(Optional.empty());

        UpdateSupportTicketStatusRequest req = UpdateSupportTicketStatusRequest.builder()
                .status(TicketStatus.RESOLVED)
                .build();

        assertThrows(NotFoundException.class, () -> service.updateStatus(5L, req));

        verify(supportTicketRepository).findById(5L);
        verifyNoInteractions(supportTicketMapper);
    }

    @Test
    void updateStatus_shouldUpdateAndReturnResponse() {
        SupportTicket ticket = SupportTicket.builder().id(5L).status(TicketStatus.OPEN).build();
        when(supportTicketRepository.findById(5L)).thenReturn(Optional.of(ticket));

        UpdateSupportTicketStatusRequest req = UpdateSupportTicketStatusRequest.builder()
                .status(TicketStatus.IN_PROGRESS)
                .build();

        SupportTicketResponse expected = SupportTicketResponse.builder().id(5L).status(TicketStatus.IN_PROGRESS).build();
        when(supportTicketMapper.toResponse(ticket)).thenReturn(expected);

        SupportTicketResponse resp = service.updateStatus(5L, req);

        assertNotNull(resp);
        assertEquals(5L, resp.getId());
        assertEquals(TicketStatus.IN_PROGRESS, ticket.getStatus());
        assertEquals(TicketStatus.IN_PROGRESS, resp.getStatus());

        verify(supportTicketRepository).findById(5L);
        verify(supportTicketMapper).toResponse(ticket);
    }
}
