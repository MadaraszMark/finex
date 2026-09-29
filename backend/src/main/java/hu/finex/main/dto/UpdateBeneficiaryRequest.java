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
@Schema(description = "Mentett kedvezményezett módosítása")
public class UpdateBeneficiaryRequest {

    @NotBlank
    @Size(max = 150)
    @Schema(description = "A kedvezményezett neve", example = "Tóth Gábor", required = true)
    private String name;

    @NotBlank
    @Size(max = 42)
    @Schema(description = "IBAN számlaszám (szóközökkel is megadható)", example = "HU66 1091 8001 0000 0489 0024 0017", required = true)
    private String accountNumber;

    @Size(max = 255)
    @Schema(description = "Saját megjegyzés", example = "Főbérlő")
    private String note;
}
