package hu.finex.main.dto;

import hu.finex.main.model.enums.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Felhasználó szerepkörének módosítása (admin)")
public class UpdateUserRoleRequest {

    @NotNull
    @Schema(description = "Az új szerepkör (USER, ADMIN)", example = "ADMIN", required = true)
    private UserRole role;
}
