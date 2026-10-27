package hu.finex.main.service;

import java.time.Instant;
import java.time.LocalDate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import hu.finex.main.dto.AuditLogResponse;
import hu.finex.main.exception.BusinessException;
import hu.finex.main.mapper.AuditLogMapper;
import hu.finex.main.model.enums.AuditAction;
import hu.finex.main.model.enums.AuditTargetType;
import hu.finex.main.repository.AuditLogRepository;
import hu.finex.main.security.CurrentUser;
import hu.finex.main.util.DateUtils;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;
    private final AuditLogMapper auditLogMapper;
    private final CurrentUser currentUser;

    // A hívó tranzakciójában (MANDATORY) naplóz, így visszagörgetett műveletről nem marad bejegyzés
    @Transactional(propagation = Propagation.MANDATORY)
    public void record(AuditAction action, AuditTargetType targetType, Long targetId, String details) {
        auditLogRepository.save(auditLogMapper.toEntity(currentUser.requireEntity(), action, targetType, targetId, details, currentIp()));
    }

    // Admin: keresés művelet, admin és időszak szerint (minden szűrő opcionális)
    @Transactional(readOnly = true)
    public Page<AuditLogResponse> search(AuditAction action, Long actorId, LocalDate from, LocalDate to, Pageable pageable) {
        if (from != null && to != null && from.isAfter(to)) {
            throw new BusinessException("A kezdő dátum nem lehet későbbi a záró dátumnál.");
        }

        Instant fromInstant = from != null ? DateUtils.startOfDay(from) : DateUtils.BEGINNING_OF_TIME;
        Instant toInstant = to != null ? DateUtils.startOfDay(to.plusDays(1)) : DateUtils.END_OF_TIME;

        return auditLogRepository.search(action, actorId, fromInstant, toInstant, pageable).map(auditLogMapper::toResponse);
    }

    // Az aktuális HTTP-kérés küldőjének IP-címe (HTTP-kérésen kívül, pl. tesztben: null)
    private String currentIp() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes) {
            return attributes.getRequest().getRemoteAddr();
        }
        return null;
    }
}
