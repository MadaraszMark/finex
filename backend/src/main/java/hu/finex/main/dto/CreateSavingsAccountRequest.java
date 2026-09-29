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

    @NotBlank
    @Size(max = 100)
    @Schema(description = "A megtakarítás neve", example = "Nyaralás", required = true)
    private String name;

    @NotNull
    @Schema(description = "A folyószámla azonosítója, amelyről a kezdő összeg érkezik", example = "3", required = true)
    private Long accountId;

    @NotNull
    @DecimalMin(value = "0.00", message = "A kezdő összeg nem lehet negatív.")
    @Schema(description = "Kezdő befizetés (lehet 0 is)", example = "50000.00", required = true)
    private BigDecimal initialDeposit;

    @DecimalMin(value = "1.00", message = "A célösszeg legalább 1 kell legyen.")
    @Schema(description = "Opcionális célösszeg", example = "600000.00")
    private BigDecimal targetAmount;
}

