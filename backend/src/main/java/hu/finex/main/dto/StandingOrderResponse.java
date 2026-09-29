package hu.finex.main.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

import hu.finex.main.model.enums.StandingOrderFrequency;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Rendszeres átutalási megbízás")
public class StandingOrderResponse {

    @Schema(description = "Azonosító", example = "2")
    private Long id;

    @Schema(description = "A forrás folyószámla azonosítója", example = "3")
    private Long accountId;

    @Schema(description = "A forrás folyószámla száma", example = "HU15117730161111101800000001")
    private String accountNumber;

    @Schema(description = "A címzett számlaszáma", example = "HU66109180010000048900240017")
    private String toAccountNumber;

    @Schema(description = "A kedvezményezett neve", example = "Tóth Gábor")
    private String partnerName;

    @Schema(description = "Összeg", example = "220000.00")
    private BigDecimal amount;

    @Schema(description = "Devizanem (a forrásszámláé)", example = "HUF")
    private String currency;

    @Schema(description = "Közlemény", example = "Albérlet")
    private String message;

    @Schema(description = "Gyakoriság", example = "MONTHLY")
    private StandingOrderFrequency frequency;

    @Schema(description = "A következő teljesítés napja", example = "2025-03-06")
    private LocalDate nextExecutionDate;

    @Schema(description = "Aktív-e a megbízás", example = "true")
    private boolean active;

    @Schema(description = "Az utolsó teljesítés időpontja", example = "2025-02-06T00:30:00Z")
    private Instant lastExecutionAt;

    @Schema(description = "Létrehozás időpontja", example = "2025-01-10T09:00:00Z")
    private Instant createdAt;
}
