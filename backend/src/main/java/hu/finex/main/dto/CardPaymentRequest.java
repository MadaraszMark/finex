package hu.finex.main.dto;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Kártyás fizetés szimulálása (demó: így keletkeznek valószerű vásárlási tételek)")
public class CardPaymentRequest {

    @NotBlank
    @Size(max = 150)
    @Schema(description = "A kereskedő neve", example = "Tesco", required = true)
    private String merchantName;

    @NotNull
    @DecimalMin(value = "0.01", message = "A fizetés összege legalább 0.01 kell legyen.")
    @Schema(description = "A fizetés összege", example = "12990.00", required = true)
    private BigDecimal amount;

    @Schema(description = "Opcionális kategória", example = "1")
    private Long categoryId;

    @Schema(description = "Online (internetes) fizetés-e", example = "false")
    private boolean online;
}
