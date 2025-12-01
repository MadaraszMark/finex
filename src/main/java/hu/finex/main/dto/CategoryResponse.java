package hu.finex.main.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Tranzakciós kategória adatai")
public class CategoryResponse {

    @Schema(description = "A kategória azonosítója", example = "12")
    private Long id;

    @Schema(description = "A kategória neve", example = "Food")
    private String name;

    @Schema(description = "Ikon vagy emoji", example = "🍕")
    private String icon;
}
