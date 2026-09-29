package hu.finex.main.dto;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
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
@Schema(description = "Megtakarítás módosítási adatai (a kamatlábat a bank határozza meg)")
public class UpdateSavingsAccountRequest {

    @NotBlank
    @Size(max = 100)
    @Schema(description = "A megtakarítás új neve", example = "Lakás célú megtakarítás", required = true)
    private String name;

    @DecimalMin(value = "1.00", message = "A célösszeg legalább 1 kell legyen.")
    @Schema(description = "Új célösszeg (üresen hagyva nincs cél)", example = "1500000.00")
    private BigDecimal targetAmount;
}
