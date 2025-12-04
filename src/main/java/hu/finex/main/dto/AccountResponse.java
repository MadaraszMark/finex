package hu.finex.main.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;

import hu.finex.main.model.enums.AccountStatus;
import hu.finex.main.model.enums.AccountType;
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
@Schema(description = "Részletes bankszámla-adatok")
public class AccountResponse {

    @Schema(description = "A számla egyedi azonosítója", example = "1001")
    private Long id;

    @Schema(description = "A számlatulajdonos felhasználó ID-ja", example = "42")
    private Long userId;

    @Schema(description = "A számla egyedi száma (IBAN jellegű)", example = "HU42117730161111101800000000")
    private String accountNumber;

    @Schema(description = "A számla egyenlege", example = "152340.75")
    private BigDecimal balance;

    @Schema(description = "Számla devizaneme", example = "EUR")
    private String currency;

    @Schema(description = "A számla típusa", example = "SAVINGS")
    private AccountType accountType;

    @Schema(description = "A számlához tartozó bankkártya (maszkolva)",example = "**** **** **** 5521")
    private String cardNumber;

    @Schema(description = "A számla státusza", example = "ACTIVE")
    private AccountStatus status;

    @Schema(description = "Létrehozás időpontja",example = "2025-02-12T11:30:22Z")
    private Instant createdAt;
}
