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
@Schema(description = "Átutalás eredménye (a címzett egyenlegét biztonsági okból nem adja vissza)")
public class TransferResponse {

    @Schema(description = "A kimenő tranzakció azonosítója", example = "5012")
    private Long transactionId;

    @Schema(description = "Forrás számla azonosítója", example = "2")
    private Long fromAccountId;

    @Schema(description = "A címzett számlaszáma", example = "HU28104000950000521700000003")
    private String toAccountNumber;

    @Schema(description = "A kedvezményezett neve", example = "Nagy Bence")
    private String partnerName;

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

    @Schema(description = "FineX-en belüli utalás volt-e (ha igen, a címzettnél azonnal jóváíródott)", example = "true")
    private boolean internal;

    @Schema(description = "Az átutalás időpontja (a tranzakciók létrejötte)", example = "2025-02-15T13:25:44Z")
    private Instant createdAt;
}

