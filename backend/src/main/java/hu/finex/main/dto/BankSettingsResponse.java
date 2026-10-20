package hu.finex.main.dto;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "A bank üzleti szabályainak paraméterei (a frontend ezekből dolgozik, nem saját másolatból)")
public class BankSettingsResponse {

    @Schema(description = "Egy számláról egy nap alatt kiutalható összeg", example = "5000000")
    private BigDecimal transferDailyLimit;

    @Schema(description = "Ennyi nem lezárt folyószámlája lehet egy felhasználónak", example = "5")
    private int maxAccountsPerUser;

    @Schema(description = "Új megtakarítások éves kamatlába (%)", example = "3.50")
    private BigDecimal savingsInterestRate;

    @Schema(description = "Új kártya alapértelmezett napi költési limitje forintszámlához", example = "200000")
    private BigDecimal cardDefaultDailyLimit;

    @Schema(description = "Új kártya alapértelmezett napi költési limitje devizaszámlához", example = "1000")
    private BigDecimal cardDefaultDailyLimitForeign;
}
