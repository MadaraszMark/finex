package hu.finex.main.dto;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Egy hónap bevételei és kiadásai (az adatbázis v_account_monthly_summary nézetéből)")
public class MonthlySummaryResponse {

    @Schema(description = "A hónap (ÉÉÉÉ-HH)", example = "2025-02")
    private String month;

    @Schema(description = "Bevételek összesen", example = "688000.00")
    private BigDecimal income;

    @Schema(description = "Kiadások összesen", example = "412350.00")
    private BigDecimal outcome;

    @Schema(description = "Tételek száma", example = "27")
    private long transactionCount;
}
