package hu.finex.main.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Új bankszámla nyitásához szükséges adatok (a bejelentkezett felhasználónak)")
public class CreateAccountRequest {

    @NotBlank
    @Size(max = 100)
    @Schema(description = "A számla megjelenítési neve",example = "Euró számla", maxLength = 100, required = true)
    private String name;

    @NotBlank
    @Pattern(regexp = "HUF|EUR|USD", message = "Támogatott devizanemek: HUF, EUR, USD.")
    @Schema(description = "A számla devizaneme ISO formátumban",example = "EUR", maxLength = 3, required = true)
    private String currency;
}
