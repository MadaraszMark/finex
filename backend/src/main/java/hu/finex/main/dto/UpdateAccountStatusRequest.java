package hu.finex.main.dto;

import hu.finex.main.model.enums.AccountStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Bankszámla státusz módosításához szükséges adatok")
public class UpdateAccountStatusRequest {

    @NotNull
    @Schema(description = "Az új státusz (ACTIVE, BLOCKED, FROZEN, CLOSED)",example = "BLOCKED")
    private AccountStatus status;
}
