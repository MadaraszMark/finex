package hu.finex.main.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SupportTicketService {

    private final SupportTicketRepository supportTicketRepository;
    private final UserRepository userRepository;
    private final SupportTicketMapper supportTicketMapper;

    @Transactional
    public SupportTicketResponse create(CreateSupportTicketRequest request) {
        User user = userRepository.findById(request.getUserId()).orElseThrow(() -> new NotFoundException("Felhasználó nem található."));

        if (supportTicketRepository.existsByUser_IdAndStatus(user.getId(), TicketStatus.OPEN)) {
            throw new BusinessException("Már van egy nyitott ticketed, kérjük várd meg a választ.");
        }
        SupportTicket ticket = supportTicketMapper.toEntity(request, user);

        ticket = supportTicketRepository.save(ticket);

        return supportTicketMapper.toResponse(ticket);
    }

    @Transactional(readOnly = true)
    public SupportTicketResponse getById(Long id) {
        SupportTicket ticket = supportTicketRepository.findById(id).orElseThrow(() -> new NotFoundException("Support ticket nem található."));

        return supportTicketMapper.toResponse(ticket);
    }

    @Transactional(readOnly = true)
    public Page<SupportTicketResponse> listByUser(Long userId, Pageable pageable) {
        userRepository.findById(userId).orElseThrow(() -> new NotFoundException("Felhasználó nem található."));

        return supportTicketRepository.findByUser_IdOrderByCreatedAtDesc(userId, pageable).map(supportTicketMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<SupportTicketResponse> listByStatus(TicketStatus status, Pageable pageable) {
        return supportTicketRepository.findByStatusOrderByCreatedAtDesc(status, pageable).map(supportTicketMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<SupportTicketResponse> listByUserAndStatus(Long userId, TicketStatus status, Pageable pageable) {
        userRepository.findById(userId).orElseThrow(() -> new NotFoundException("Felhasználó nem található."));

        return supportTicketRepository.findByUser_IdAndStatusOrderByCreatedAtDesc(userId, status, pageable).map(supportTicketMapper::toResponse);
    }

    @Transactional
    public SupportTicketResponse updateStatus(Long id, UpdateSupportTicketStatusRequest request) {
        SupportTicket ticket = supportTicketRepository.findById(id).orElseThrow(() -> new NotFoundException("Support ticket nem található."));

        ticket.setStatus(request.getStatus());

        return supportTicketMapper.toResponse(ticket);
    }
}
