package hu.finex.main.dto;

import hu.finex.main.model.enums.UserStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Felhasználó letiltása vagy feloldása (admin)")
public class UpdateUserStatusRequest {

    @NotNull
    @Schema(description = "Az új státusz (ACTIVE, BLOCKED)", example = "BLOCKED", required = true)
    private UserStatus status;
}
