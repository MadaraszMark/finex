package hu.finex.main.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Egyenleg feltöltéséhez (befizetéshez) szükséges adatok")
public class DepositRequest {

    @NotNull
    @Positive
    @Schema(description = "Befizetni kívánt összeg (pozitív szám kötelező)", example = "25000.00", required = true)
    private BigDecimal amount;

    @Schema(description = "Opcionális megjegyzés a befizetéshez", example = "ATM befizetés")
    private String message;
}
