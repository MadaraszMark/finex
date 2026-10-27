package hu.finex.main.dto;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Egy eltérés a főkönyvi egyeztetésben")
public class ReconciliationItemResponse {

    @Schema(description = "ACCOUNT (folyószámla) vagy SAVINGS (megtakarítás)", example = "ACCOUNT")
    private String kind;

    @Schema(description = "A számla vagy megtakarítás azonosítója", example = "3")
    private Long id;

    @Schema(description = "Számlaszám vagy a megtakarítás neve", example = "HU15117730161111101800000001")
    private String label;

    @Schema(description = "Devizanem", example = "HUF")
    private String currency;

    @Schema(description = "A tárolt egyenleg", example = "150000.00")
    private BigDecimal storedBalance;

    @Schema(description = "A könyvelt tételekből számolt egyenleg", example = "149000.00")
    private BigDecimal ledgerBalance;

    @Schema(description = "Eltérés (tárolt - számolt)", example = "1000.00")
    private BigDecimal difference;
}
