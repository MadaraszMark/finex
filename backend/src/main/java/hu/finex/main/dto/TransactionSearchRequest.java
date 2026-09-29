package hu.finex.main.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import hu.finex.main.model.enums.TransactionType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

// Tranzakciókeresés szűrői (query paraméterek), mindegyik opcionális

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Tranzakciókeresés szűrői")
public class TransactionSearchRequest {

    @Schema(description = "Csak ennek a (saját) számlának a tételei", example = "3")
    private Long accountId;

    @Schema(description = "Tranzakció típusa", example = "OUTCOME")
    private TransactionType type;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @Schema(description = "Ettől a naptól (budapesti idő szerint)", example = "2025-02-01")
    private LocalDate from;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @Schema(description = "Eddig a napig, a nap végéig", example = "2025-02-28")
    private LocalDate to;

    @Schema(description = "Szabad szavas keresés a megjegyzésben és a partner nevében", example = "tesco")
    private String search;

    @Schema(description = "Csak ennek a kategóriának a tételei", example = "1")
    private Long categoryId;

    @Schema(description = "Minimális összeg", example = "5000")
    private BigDecimal minAmount;

    @Schema(description = "Maximális összeg", example = "50000")
    private BigDecimal maxAmount;
}
