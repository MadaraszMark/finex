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
@Schema(description = "Könnyített belépési napló elem listázáshoz")
public class LoginLogListItemResponse {

    @Schema(description = "Belépési státusz", example = "SUCCESS")
    private LoginStatus status;

    @Schema(description = "IP cím", example = "10.0.0.5")
    private String ipAddress;

    @Schema(description = "Kliens user-agent (böngésző, eszköz)", example = "Mozilla/5.0 (iPhone; CPU iPhone OS 17_2 like Mac OS X)")
    private String userAgent;

    @Schema(description = "Sikertelen belépés oka (ha van)", example = "Hibás jelszó")
    private String failureReason;

    @Schema(description = "Időbélyeg", example = "2025-02-12T14:22:10Z")
    private Instant createdAt;
}
