package hu.finex.main.dto;

import java.math.BigDecimal;
import java.time.Instant;

import hu.finex.main.model.enums.SavingsTransactionType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "A megtakarítási számla egy mozgása")
public class SavingsTransactionResponse {

    @Schema(description = "A tétel azonosítója", example = "31")
    private Long id;

    @Schema(description = "Típus (DEPOSIT, WITHDRAWAL, INTEREST)", example = "INTEREST")
    private SavingsTransactionType type;

    @Schema(description = "Összeg", example = "437.50")
    private BigDecimal amount;

    @Schema(description = "A megtakarítás egyenlege a tétel után", example = "150437.50")
    private BigDecimal balanceAfter;

    @Schema(description = "Időpont", example = "2025-03-01T01:00:00Z")
    private Instant createdAt;
}
