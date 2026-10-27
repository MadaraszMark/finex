package hu.finex.main.mapper;

import org.springframework.stereotype.Component;

import hu.finex.main.dto.AuditLogResponse;
import hu.finex.main.model.AuditLog;
import hu.finex.main.model.User;
import hu.finex.main.model.enums.AuditAction;
import hu.finex.main.model.enums.AuditTargetType;

@Component
public class AuditLogMapper {

    // A részletek max. 1000 karakter (az oszlop mérete), a hosszabb szöveg levágva kerül a naplóba
    public AuditLog toEntity(User actor, AuditAction action, AuditTargetType targetType, Long targetId, String details, String ipAddress) {
        return AuditLog.builder()
                .actor(actor)
                .actorEmail(actor.getEmail())
                .action(action)
                .targetType(targetType)
                .targetId(targetId)
                .details(details != null && details.length() > 1000 ? details.substring(0, 1000) : details)
                .ipAddress(ipAddress)
                .build();
    }

    public AuditLogResponse toResponse(AuditLog log) {
        return AuditLogResponse.builder()
                .id(log.getId())
                .actorId(log.getActor() != null ? log.getActor().getId() : null)
                .actorEmail(log.getActorEmail())
                .action(log.getAction())
                .targetType(log.getTargetType())
                .targetId(log.getTargetId())
                .details(log.getDetails())
                .ipAddress(log.getIpAddress())
                .createdAt(log.getCreatedAt())
                .build();
    }
}
