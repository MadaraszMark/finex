package hu.finex.main.dto;

import java.math.BigDecimal;

import hu.finex.main.model.enums.SavingsStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
@Schema(description = "Megtakarítás módosítási adatai")
public class UpdateSavingsAccountRequest {

    @Size(max = 100)
    @Schema(description = "A megtakarítás új neve", example = "Lakás célú megtakarítás")
    private String name;

    @DecimalMin(value = "0.00", message = "A kamatláb nem lehet negatív.")
    @Schema(description = "Új kamatláb (%)", example = "3.2")
    private BigDecimal interestRate;

    @NotNull
    @Schema(description = "Megtakarítás új státusza", example = "ACTIVE")
    private SavingsStatus status;
}
