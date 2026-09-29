package hu.finex.main.dto;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Kártyalimitek és az online fizetés beállítása")
public class UpdateCardLimitsRequest {

    @NotNull
    @DecimalMin(value = "0.00", message = "A napi limit nem lehet negatív.")
    @DecimalMax(value = "5000000", message = "A napi limit legfeljebb 5 000 000 lehet.")
    @Schema(description = "Új napi költési limit", example = "150000.00", required = true)
    private BigDecimal dailyLimit;

    @NotNull
    @Schema(description = "Engedélyezett-e az online fizetés", example = "false", required = true)
    private Boolean onlinePaymentEnabled;
}
