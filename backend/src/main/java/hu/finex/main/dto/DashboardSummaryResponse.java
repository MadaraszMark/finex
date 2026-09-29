package hu.finex.main.dto;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "A főoldal összesítő adatai egyetlen hívással")
public class DashboardSummaryResponse {

    @Schema(description = "Devizanem, amelyben az összegek szerepelnek", example = "HUF")
    private String currency;

    @Schema(description = "A folyószámlák egyenlege összesen", example = "1906620.00")
    private BigDecimal totalBalance;

    @Schema(description = "A megtakarítások egyenlege összesen", example = "333517.93")
    private BigDecimal savingsBalance;

    @Schema(description = "E havi bevételek", example = "688000.00")
    private BigDecimal monthIncome;

    @Schema(description = "E havi kiadások", example = "412350.00")
    private BigDecimal monthOutcome;

    @Schema(description = "Nem lezárt számlák száma", example = "2")
    private long accountCount;

    @Schema(description = "Aktív kártyák száma", example = "2")
    private long activeCardCount;

    @Schema(description = "Olvasatlan értesítések száma", example = "3")
    private long unreadNotificationCount;
}
