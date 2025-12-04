package hu.finex.main.dto;

import java.time.Instant;

import hu.finex.main.model.enums.LoginStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Felhasználói belépési esemény naplózott adatai")
public class LoginLogResponse {

    @Schema(description = "A belépési napló egyedi azonosítója", example = "5501")
    private Long id;

    @Schema(description = "A felhasználó azonosítója", example = "42")
    private Long userId;

    @Schema(description = "Belépés státusza (SUCCESS vagy FAILED)", example = "FAILED")
    private LoginStatus status;

    @Schema(description = "A felhasználó IP címe a belépési kísérlet során", example = "192.168.1.11")
    private String ipAddress;

    @Schema(description = "Kliens user-agent információ", example = "Mozilla/5.0 (iPhone; CPU iPhone OS 17_2 like Mac OS X)")
    private String userAgent;

    @Schema(description = "Sikertelen belépés oka (ha van)", example = "Invalid password")
    private String failureReason;

    @Schema(description = "A belépési esemény időpontja", example = "2025-02-12T14:22:10Z")
    private Instant createdAt;
}

