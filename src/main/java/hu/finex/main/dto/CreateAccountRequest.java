package hu.finex.main.dto;

import hu.finex.main.model.enums.AccountType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Új bankszámla létrehozásához szükséges adatok")
public class CreateAccountRequest {

    @NotNull
    @Schema(description = "A számlát birtokló felhasználó azonosítója",example = "42", required = true)
    private Long userId;

    @NotNull
    @Size(max = 3)
    @Schema(description = "A számla devizaneme ISO formátumban",example = "HUF", maxLength = 3, required = true)
    private String currency;

    @NotNull
    @Schema(description = "A számla típusa (CURRENT, SAVINGS, CREDIT)",example = "CURRENT", required = true)
    private AccountType accountType;
}
