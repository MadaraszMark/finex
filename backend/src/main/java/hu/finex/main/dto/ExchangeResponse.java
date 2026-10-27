package hu.finex.main.dto;

import java.math.BigDecimal;
import java.time.Instant;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "A devizaváltás eredménye")
public class ExchangeResponse {

    @Schema(description = "A terhelés tételének azonosítója", example = "166")
    private Long transactionId;

    @Schema(description = "Forrásszámla", example = "1")
    private Long fromAccountId;

    @Schema(description = "Célszámla", example = "2")
    private Long toAccountId;

    @Schema(description = "Terhelt összeg", example = "100000")
    private BigDecimal fromAmount;

    @Schema(description = "A terhelt összeg devizaneme", example = "HUF")
    private String fromCurrency;

    @Schema(description = "Jóváírt összeg", example = "253.16")
    private BigDecimal toAmount;

    @Schema(description = "A jóváírt összeg devizaneme", example = "EUR")
    private String toCurrency;

    @Schema(description = "Árfolyam: 1 egység forrásdeviza ennyi céldeviza", example = "0.002532")
    private BigDecimal rate;

    @Schema(description = "A forrásszámla új egyenlege", example = "1809000")
    private BigDecimal fromAccountNewBalance;

    @Schema(description = "A célszámla új egyenlege", example = "1167.67")
    private BigDecimal toAccountNewBalance;

    @Schema(description = "Időpont", example = "2026-10-26T20:30:00Z")
    private Instant createdAt;
}
