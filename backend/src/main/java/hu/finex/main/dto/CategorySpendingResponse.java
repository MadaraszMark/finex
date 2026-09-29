package hu.finex.main.dto;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

// A mezők sorrendje számít: a JPQL "select new ...(...)" ezzel a konstruktorral hozza létre

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Költés egy kategóriában egy időszakban")
public class CategorySpendingResponse {

    @Schema(description = "A kategória azonosítója", example = "1")
    private Long categoryId;

    @Schema(description = "A kategória neve", example = "Élelmiszer")
    private String categoryName;

    @Schema(description = "A kategória ikonja", example = "shopping-cart")
    private String categoryIcon;

    @Schema(description = "Elköltött összeg", example = "86400.00")
    private BigDecimal totalAmount;

    @Schema(description = "Tételek száma", example = "9")
    private Long transactionCount;
}
