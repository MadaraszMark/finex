package hu.finex.main.dto;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Devizaváltás két saját számla között")
public class ExchangeRequest {

    @NotNull
    @Schema(description = "A terhelt (forrás) számla azonosítója", example = "1", required = true)
    private Long fromAccountId;

    @NotNull
    @Schema(description = "A jóváírt (cél) számla azonosítója", example = "2", required = true)
    private Long toAccountId;

    @NotNull
    @DecimalMin(value = "0.01", message = "Az összeg legalább 0.01 kell legyen.")
    @Digits(integer = 16, fraction = 2, message = "Az összeg legfeljebb két tizedesjegyet tartalmazhat.")
    @Schema(description = "A váltandó összeg a forrásszámla devizanemében", example = "100000", required = true)
    private BigDecimal amount;
}
