package hu.finex.main.dto;

import java.math.BigDecimal;
import java.time.Instant;

import hu.finex.main.model.enums.TransactionType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "A számlakivonat egy sora (az adatbázis account_statement függvényéből)")
public class StatementItemResponse {

    @Schema(description = "A tranzakció azonosítója", example = "5012")
    private Long transactionId;

    @Schema(description = "Könyvelés időpontja", example = "2025-02-15T11:00:15Z")
    private Instant createdAt;

    @Schema(description = "A tranzakció típusa", example = "OUTCOME")
    private TransactionType type;

    @Schema(description = "A másik fél neve", example = "Tesco")
    private String partnerName;

    @Schema(description = "Megjegyzés", example = "Bevásárlás")
    private String message;

    @Schema(description = "Összeg (mindig pozitív)", example = "7500.00")
    private BigDecimal amount;

    @Schema(description = "Előjeles összeg (jóváírás +, terhelés -)", example = "-7500.00")
    private BigDecimal signedAmount;

    @Schema(description = "Egyenleg a tétel után", example = "142500.00")
    private BigDecimal runningBalance;
}
