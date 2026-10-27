package hu.finex.main.dto;

import java.time.Instant;

import hu.finex.main.model.enums.AuditAction;
import hu.finex.main.model.enums.AuditTargetType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Admin audit napló bejegyzése")
public class AuditLogResponse {

    @Schema(description = "Azonosító", example = "12")
    private Long id;

    @Schema(description = "A műveletet végző admin azonosítója", example = "3")
    private Long actorId;

    @Schema(description = "A műveletet végző admin e-mail címe", example = "admin@finex.hu")
    private String actorEmail;

    @Schema(description = "A művelet", example = "USER_STATUS_CHANGED")
    private AuditAction action;

    @Schema(description = "Az érintett objektum típusa", example = "USER")
    private AuditTargetType targetType;

    @Schema(description = "Az érintett objektum azonosítója", example = "42")
    private Long targetId;

    @Schema(description = "Részletek", example = "bence@finex.hu: ACTIVE → BLOCKED")
    private String details;

    @Schema(description = "Az admin IP-címe", example = "192.168.1.10")
    private String ipAddress;

    @Schema(description = "Időpont", example = "2026-10-26T19:30:00Z")
    private Instant createdAt;
}
