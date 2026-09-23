package hu.finex.main.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Tranzakcióhoz rendelt kategória részletes adatai")
public class TransactionCategoryResponse {

    @Schema(description = "A kapcsolat egyedi azonosítója", example = "120")
    private Long id;

    @Schema(description = "A tranzakció azonosítója", example = "5012")
    private Long transactionId;

    @Schema(description = "A hozzárendelt kategória azonosítója", example = "3")
    private Long categoryId;

    @Schema(description = "A kategória neve", example = "Food")
    private String categoryName;

    @Schema(description = "A kategória ikonja", example = "🍔")
    private String categoryIcon;
}
