package hu.finex.main.service;

import hu.finex.main.exception.BusinessException;
import hu.finex.main.mapper.AuditLogMapper;
import hu.finex.main.model.AuditLog;
import hu.finex.main.model.User;
import hu.finex.main.model.enums.AuditAction;
import hu.finex.main.model.enums.AuditTargetType;
import hu.finex.main.repository.AuditLogRepository;
import hu.finex.main.security.CurrentUser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditServiceTest {

    @Mock private AuditLogRepository auditLogRepository;
    @Spy private AuditLogMapper auditLogMapper = new AuditLogMapper();
    @Mock private CurrentUser currentUser;

    @InjectMocks private AuditService service;

    @Test
    void record_shouldSaveTheActingAdmin_andTheDetails() {
        User admin = User.builder().id(3L).email("admin@finex.hu").build();
        when(currentUser.requireEntity()).thenReturn(admin);

        service.record(AuditAction.USER_STATUS_CHANGED, AuditTargetType.USER, 42L, "bence@finex.hu: ACTIVE → BLOCKED");

        ArgumentCaptor<AuditLog> saved = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(saved.capture());
        assertEquals(admin, saved.getValue().getActor());
        assertEquals("admin@finex.hu", saved.getValue().getActorEmail());
        assertEquals(AuditAction.USER_STATUS_CHANGED, saved.getValue().getAction());
        assertEquals(AuditTargetType.USER, saved.getValue().getTargetType());
        assertEquals(42L, saved.getValue().getTargetId());
        assertEquals("bence@finex.hu: ACTIVE → BLOCKED", saved.getValue().getDetails());
    }

    @Test
    void mapper_shouldTruncateDetailsTo1000Characters() {
        User admin = User.builder().id(3L).email("admin@finex.hu").build();

        AuditLog log = auditLogMapper.toEntity(admin, AuditAction.JOB_RUN, AuditTargetType.JOB, null, "x".repeat(1500), "10.0.0.1");

        assertEquals(1000, log.getDetails().length());
        assertEquals("10.0.0.1", log.getIpAddress());
    }

    @Test
    void search_shouldThrowBusinessException_whenFromIsAfterTo() {
        assertThrows(BusinessException.class,
                () -> service.search(null, null, LocalDate.of(2026, 5, 2), LocalDate.of(2026, 5, 1), PageRequest.of(0, 10)));

        verifyNoInteractions(auditLogRepository);
    }
}
