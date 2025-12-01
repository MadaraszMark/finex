package hu.finex.main.dto;

import java.time.OffsetDateTime;

import hu.finex.main.model.enums.LoginStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

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

    @Schema(description = "Időbélyeg", example = "2025-02-12T14:22:10Z")
    private OffsetDateTime createdAt;
}
