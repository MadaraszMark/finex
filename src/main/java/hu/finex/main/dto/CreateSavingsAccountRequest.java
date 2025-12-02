package hu.finex.main.dto;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
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
@Schema(description = "Új megtakarítás létrehozásához szükséges adatok")
public class CreateSavingsAccountRequest {

    @NotNull
    @Schema(description = "A felhasználó azonosítója", example = "12")
    private Long userId;

    @NotBlank
    @Size(max = 100)
    @Schema(description = "A megtakarítás neve", example = "Havi megtakarítás")
    private String name;

    @NotNull
    @DecimalMin(value = "0.00", message = "A kezdő egyenleg nem lehet negatív.")
    @Schema(description = "Kezdő egyenleg", example = "50000.00")
    private BigDecimal initialBalance;

    @NotBlank
    @Size(max = 3)
    @Schema(description = "Devizanem", example = "HUF")
    private String currency;

    @NotNull
    @DecimalMin(value = "0.00", message = "A kamatláb nem lehet negatív.")
    @Schema(description = "Éves kamatláb (%)", example = "2.5")
    private BigDecimal interestRate;
}

