package hu.finex.main.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Bankszámlaszám története egyszerűsített adatai lista vagy grafikon megjelenítéshez")
public class BalanceHistoryListItemResponse {

    @Schema(description = "A rögzített egyenleg értéke", example = "154320.50")
    private BigDecimal balance;

    @Schema(description = "A rögzítés időpontja", example = "2025-02-12T10:15:30Z")
    private OffsetDateTime createdAt;
}
