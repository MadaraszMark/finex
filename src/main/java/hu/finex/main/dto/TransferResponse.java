package hu.finex.main.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Két bankszámla közötti átutalás eredménye")
public class TransferResponse {

    @Schema(description = "Forrás számla azonosítója", example = "2")
    private Long fromAccountId;

    @Schema(description = "Cél számla azonosítója", example = "3")
    private Long toAccountId;

    @Schema(description = "Átutalt összeg", example = "15000.00")
    private BigDecimal amount;

    @Schema(description = "Devizanem", example = "HUF")
    private String currency;

    @Schema(description = "Megjegyzés az átutaláshoz", example = "Közös vacsi")
    private String message;

    @Schema(description = "Az átutaláshoz tartozó kategóriák (pl. Étterem, Szórakozás)")
    private List<CategoryResponse> categories;

    @Schema(description = "Forrás számla új egyenlege", example = "85000.00")
    private BigDecimal fromAccountNewBalance;

    @Schema(description = "Cél számla új egyenlege", example = "120000.00")
    private BigDecimal toAccountNewBalance;

    @Schema(description = "Az átutalás időpontja (a tranzakciók létrejötte)", example = "2025-02-15T13:25:44Z")
    private Instant createdAt;
}

