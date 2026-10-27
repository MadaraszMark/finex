package hu.finex.main.dto;

import java.math.BigDecimal;
import java.util.Map;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "A bank üzleti szabályainak paraméterei (a frontend ezekből dolgozik, nem saját másolatból)")
public class BankSettingsResponse {

    @Schema(description = "Egy számláról egy nap alatt kiutalható összeg forintban (devizaszámlán az árfolyamon számolt megfelelője)", example = "5000000")
    private BigDecimal transferDailyLimit;

    @Schema(description = "Ennyi nem lezárt folyószámlája lehet egy felhasználónak", example = "5")
    private int maxAccountsPerUser;

    @Schema(description = "Ennyi nem lezárt megtakarítása lehet egy felhasználónak", example = "10")
    private int maxSavingsPerUser;

    @Schema(description = "Ennyi aktív rendszeres átutalása lehet egy felhasználónak", example = "20")
    private int maxStandingOrdersPerUser;

    @Schema(description = "Ennyi kedvezményezettet menthet egy felhasználó", example = "50")
    private int maxBeneficiariesPerUser;

    @Schema(description = "Új megtakarítások éves kamatlába (%)", example = "3.50")
    private BigDecimal savingsInterestRate;

    @Schema(description = "Új kártya alapértelmezett napi költési limitje forintszámlához", example = "200000")
    private BigDecimal cardDefaultDailyLimit;

    @Schema(description = "Új kártya alapértelmezett napi költési limitje devizaszámlához", example = "1000")
    private BigDecimal cardDefaultDailyLimitForeign;

    @Schema(description = "A kártya napi limitjének felső határa forintban (devizakártyánál az árfolyamon számolt megfelelője)", example = "5000000")
    private BigDecimal cardMaxDailyLimit;

    @Schema(description = "Devizaárfolyamok: 1 egység = ennyi forint", example = "{\"HUF\": 1, \"EUR\": 395.00, \"USD\": 362.00}")
    private Map<String, BigDecimal> exchangeRates;
}
