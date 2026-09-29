package hu.finex.main.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Számlakivonat egy időszakra: nyitó- és záróegyenleg, összesítők és a tételek futó egyenleggel")
public class StatementResponse {

    @Schema(description = "A számla azonosítója", example = "3")
    private Long accountId;

    @Schema(description = "A számla száma", example = "HU15117730161111101800000001")
    private String accountNumber;

    @Schema(description = "Devizanem", example = "HUF")
    private String currency;

    @Schema(description = "Az időszak első napja", example = "2025-02-01")
    private LocalDate from;

    @Schema(description = "Az időszak utolsó napja (a nap végéig)", example = "2025-02-28")
    private LocalDate to;

    @Schema(description = "Nyitóegyenleg az időszak elején", example = "150000.00")
    private BigDecimal openingBalance;

    @Schema(description = "Záróegyenleg az időszak végén", example = "182300.00")
    private BigDecimal closingBalance;

    @Schema(description = "Jóváírások összesen", example = "685000.00")
    private BigDecimal totalIncome;

    @Schema(description = "Terhelések összesen", example = "652700.00")
    private BigDecimal totalOutcome;

    @Schema(description = "A tételek időrendben")
    private List<StatementItemResponse> items;
}
