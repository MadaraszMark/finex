package hu.finex.main.dto;

import java.math.BigDecimal;

import hu.finex.main.model.enums.TransactionType;
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
@Schema(description = "Megtakarítási tranzakció könyveléséhez szükséges adatok (belső használat)")
public class CreateSavingsTransactionRequest {

    @NotNull
    @Schema(description = "Folyószámla azonosítója, amelyen a tranzakció könyvelésre kerül", example = "3")
    private Long accountId;

    @NotNull
    @Schema(description = "Tranzakció típusa (INCOME / OUTCOME)", example = "OUTCOME")
    private TransactionType type;

    @NotNull
    @DecimalMin(value = "0.01", message = "Az összegnek pozitívnak kell lennie.")
    @Schema(description = "A tranzakció összege", example = "10000.00")
    private BigDecimal amount;

    @NotBlank
    @Size(max = 3)
    @Schema(description = "A pénznem", example = "HUF")
    private String currency;

    @Size(max = 255)
    @Schema(description = "Megjegyzés / tranzakció leírása", example = "Savings deposit to: Lakástakarék")
    private String message;

    @Size(max = 34)
    @Schema(description = "Forrás számlaszám", example = "HU12345678901234567812345678")
    private String fromAccount;

    @Size(max = 34)
    @Schema(description = "Cél számlaszám", example = "HU98765432109876543210987654")
    private String toAccount;
}
