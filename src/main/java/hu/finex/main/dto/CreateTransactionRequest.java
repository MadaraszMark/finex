package hu.finex.main.dto;

import java.math.BigDecimal;

import hu.finex.main.model.enums.TransactionType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Új tranzakció létrehozásához szükséges adatok")
public class CreateTransactionRequest {

    @NotNull
    @Schema(description = "A tranzakcióhoz tartozó számla azonosítója",example = "102",required = true)
    private Long accountId;

    @NotNull
    @Schema(description = "A tranzakció típusa",example = "TRANSFER_OUT",required = true)
    private TransactionType type;

    @NotNull
    @Schema(description = "A tranzakció összege",example = "15000.00",required = true)
    private BigDecimal amount;

    @Size(max = 255)
    @Schema(description = "Megjegyzés a tranzakcióhoz",example = "Számlafizetés - Telekom")
    private String message;

    @Size(max = 34)
    @Schema(description = "Küldő számlaszám (átutalásnál kötelező)", example = "HU42117730161111101800000000")
    private String fromAccount;

    @Size(max = 34)
    @Schema(description = "Fogadó számlaszám (átutalásnál kötelező)",example = "HU10101000001234567890000000")
    private String toAccount;

    @NotBlank
    @Size(max = 3)
    @Schema(description = "A tranzakció devizaneme",example = "HUF",required = true)
    private String currency;
}

