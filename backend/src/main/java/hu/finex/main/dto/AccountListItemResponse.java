package hu.finex.main.dto;

import java.math.BigDecimal;

import hu.finex.main.model.enums.AccountStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Bankszámla egyszerűsített adatai listázáshoz")
public class AccountListItemResponse {

    @Schema(description = "A számla azonosítója", example = "1001")
    private Long id;

    @Schema(description = "A számla száma", example = "HU42117730161111101800000000")
    private String accountNumber;

    @Schema(description = "Aktuális egyenleg", example = "98500.00")
    private BigDecimal balance;

    @Schema(description = "Devizanem", example = "HUF")
    private String currency;

    @Schema(description = "Számla státusza", example = "ACTIVE")
    private AccountStatus status;
}
