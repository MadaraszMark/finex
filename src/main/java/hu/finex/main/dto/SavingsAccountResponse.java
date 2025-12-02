package hu.finex.main.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import hu.finex.main.model.enums.SavingsStatus;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Megtakarítás részletes adatai (response)")
public class SavingsAccountResponse {

    @Schema(description = "Megtakarítás azonosítója", example = "5")
    private Long id;

    @Schema(description = "Felhasználó azonosítója", example = "12")
    private Long userId;

    @Schema(description = "Megtakarítás neve", example = "Havi megtakarítás")
    private String name;

    @Schema(description = "Aktuális egyenleg", example = "83000.00")
    private BigDecimal balance;

    @Schema(description = "Deviza", example = "HUF")
    private String currency;

    @Schema(description = "Kamatláb (%)", example = "2.5")
    private BigDecimal interestRate;

    @Schema(description = "Státusz", example = "ACTIVE")
    private SavingsStatus status;

    @Schema(description = "Létrehozás dátuma")
    private OffsetDateTime createdAt;

    @Schema(description = "Utolsó módosítás dátuma")
    private OffsetDateTime updatedAt;
}
