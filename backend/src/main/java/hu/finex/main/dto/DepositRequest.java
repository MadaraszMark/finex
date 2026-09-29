package hu.finex.main.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
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
    @DecimalMax(value = "10000000", message = "Egy befizetés legfeljebb 10 000 000 lehet.")
    @Schema(description = "Befizetni kívánt összeg (pozitív szám kötelező)", example = "25000.00", required = true)
    private BigDecimal amount;

    @Size(max = 255)
    @Schema(description = "Opcionális megjegyzés a befizetéshez", example = "ATM befizetés")
    private String message;
}
