package hu.finex.main.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Új tranzakciós kategória létrehozásához szükséges adatok")
public class CreateCategoryRequest {

    @NotBlank
    @Size(max = 100)
    @Schema(description = "A kategória neve (pl. Food, Shopping, Bills)",example = "Food", maxLength = 100, required = true)
    private String name;

    @Size(max = 20)
    @Schema(description = "Ikon / emoji a kategóriához",example = "🍔", maxLength = 20)
    private String icon;
}
