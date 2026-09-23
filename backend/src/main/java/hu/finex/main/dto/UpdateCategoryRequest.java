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
@Schema(description = "Tranzakciós kategória módosításához szükséges adatok")
public class UpdateCategoryRequest {

    @NotBlank
    @Size(max = 100)
    @Schema(description = "A kategória új neve",example = "Transportation", maxLength = 100)
    private String name;

    @Size(max = 20)
    @Schema(description = "Új emoji vagy ikon",example = "🍔", maxLength = 20)
    private String icon;
}

