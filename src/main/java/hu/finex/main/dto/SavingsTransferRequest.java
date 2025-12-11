package hu.finex.main.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Folyószámla és megtakarítási számla közötti átvezetés kérése")
public class SavingsTransferRequest {

    @NotNull
    @Schema(description = "A CURRENT (folyószámla) azonosítója, ahonnan vagy ahova a pénz mozog",example = "3", required = true)
    private Long accountId;

    @NotNull
    @Min(1)
    @Schema(description = "Az átvezetendő összeg (1 vagy több)",example = "20000", required = true)
    private BigDecimal amount;

    @Schema(description = "Opcionális megjegyzés",example = "Havi megtakarítás")
    private String message;
}
