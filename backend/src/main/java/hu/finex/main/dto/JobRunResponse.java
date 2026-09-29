package hu.finex.main.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Egy kézzel indított ütemezett feladat eredménye (admin)")
public class JobRunResponse {

    @Schema(description = "A feladat neve", example = "Havi kamatjóváírás")
    private String job;

    @Schema(description = "Sikeresen feldolgozott tételek", example = "12")
    private int processed;

    @Schema(description = "Kihagyott vagy sikertelen tételek", example = "1")
    private int skipped;
}
