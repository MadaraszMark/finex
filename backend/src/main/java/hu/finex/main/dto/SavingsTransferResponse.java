package hu.finex.main.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Megtakarítás és folyószámla közötti átvezetés eredménye")
public class SavingsTransferResponse {

    @Schema(description = "A megtakarítási számla azonosítója", example = "5")
    private Long savingsAccountId;

    @Schema(description = "A folyószámla azonosítója", example = "3")
    private Long accountId;

    @Schema(description = "A megtakarítási számla új egyenlege", example = "60000.00")
    private BigDecimal savingsNewBalance;

    @Schema(description = "A folyószámla új egyenlege", example = "10000.00")
    private BigDecimal accountNewBalance;

    @Schema(description = "Megjegyzés", example = "Havi megtakarítás utalás")
    private String message;

    @Schema(description = "A tranzakció időbélyege")
    private Instant createdAt;
}
