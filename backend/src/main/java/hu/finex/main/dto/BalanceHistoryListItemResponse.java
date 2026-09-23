package hu.finex.main.dto;

import java.math.BigDecimal;
import java.time.Instant;

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
@Schema(description = "Bankszámlaszám története egyszerűsített adatai lista vagy grafikon megjelenítéshez")
public class BalanceHistoryListItemResponse {

    @Schema(description = "A rögzített egyenleg értéke", example = "154320.50")
    private BigDecimal balance;

    @Schema(description = "A rögzítés időpontja", example = "2025-02-12T10:15:30Z")
    private Instant createdAt;
}
