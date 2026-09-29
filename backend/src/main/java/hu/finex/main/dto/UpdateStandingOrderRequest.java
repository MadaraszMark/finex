package hu.finex.main.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import hu.finex.main.model.enums.StandingOrderFrequency;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Rendszeres átutalás módosítása (a címzett nem változtatható, ahhoz új megbízás kell)")
public class UpdateStandingOrderRequest {

    @NotNull
    @DecimalMin(value = "0.01", message = "Az összeg legalább 0.01 kell legyen.")
    @Schema(description = "Az utalandó összeg", example = "230000.00", required = true)
    private BigDecimal amount;

    @Size(max = 255)
    @Schema(description = "Közlemény", example = "Albérlet + rezsi")
    private String message;

    @NotNull
    @Schema(description = "Gyakoriság (WEEKLY, MONTHLY)", example = "MONTHLY", required = true)
    private StandingOrderFrequency frequency;

    @NotNull
    @FutureOrPresent(message = "A következő teljesítés nem lehet a múltban.")
    @Schema(description = "A következő teljesítés napja", example = "2025-04-06", required = true)
    private LocalDate nextExecutionDate;
}
