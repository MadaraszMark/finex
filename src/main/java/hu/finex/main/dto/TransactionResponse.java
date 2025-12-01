package hu.finex.main.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import hu.finex.main.model.enums.TransactionType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Tranzakció részletes adatai")
public class TransactionResponse {

    @Schema(description = "A tranzakció azonosítója", example = "5012")
    private Long id;

    @Schema(description = "A tranzakcióhoz tartozó számla ID-ja", example = "102")
    private Long accountId;

    @Schema(description = "A tranzakció típusa", example = "INCOME")
    private TransactionType type;

    @Schema(description = "A tranzakció összege", example = "300000.00")
    private BigDecimal amount;

    @Schema(description = "Megjegyzés", example = "Fizetés a munkahelytől")
    private String message;

    @Schema(description = "Küldő számlaszám", example = "HU42117730161111101800000000")
    private String fromAccount;

    @Schema(description = "Fogadó számlaszám", example = "HU10101000001234567890000000")
    private String toAccount;

    @Schema(description = "Devizanem", example = "EUR")
    private String currency;

    @Schema(description = "A tranzakció létrejöttének időpontja",example = "2025-02-15T13:25:44Z")
    private OffsetDateTime createdAt;
}
