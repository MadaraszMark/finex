package hu.finex.main.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Tranzakcióhoz tartozó kategória egyszerűsített adatai")
public class TransactionCategoryListItemResponse {

    @Schema(description = "A tranzakció és a kategória kapcsolatának azonosítója (ezzel törölhető a hozzárendelés)", example = "41")
    private Long id;

    @Schema(description = "A kategória azonosítója", example = "3")
    private Long categoryId;

    @Schema(description = "A kategória neve", example = "Shopping")
    private String categoryName;

    @Schema(description = "Ikon / emoji", example = "🛍️")
    private String categoryIcon;
}

