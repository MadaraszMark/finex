package hu.finex.main.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import hu.finex.main.model.enums.TransactionType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Tranzakció egyszerűsített adatai lista megjelenítéshez")
public class TransactionListItemResponse {

    @Schema(description = "A tranzakció azonosítója", example = "5012")
    private Long id;

    @Schema(description = "A tranzakció típusa", example = "OUTCOME")
    private TransactionType type;

    @Schema(description = "Összeg", example = "7500.00")
    private BigDecimal amount;

    @Schema(description = "Megjegyzés", example = "Kávézó")
    private String message;

    @Schema(description = "Devizanem", example = "HUF")
    private String currency;

    @Schema(description = "Időpont", example = "2025-02-15T11:00:15Z")
    private OffsetDateTime createdAt;
}

