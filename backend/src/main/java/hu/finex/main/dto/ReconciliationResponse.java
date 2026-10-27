package hu.finex.main.dto;

import java.time.Instant;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Főkönyvi egyeztetés eredménye: a tárolt egyenlegek megegyeznek-e a könyvelt tételekkel")
public class ReconciliationResponse {

    @Schema(description = "Az ellenőrzés időpontja", example = "2026-10-26T20:30:00Z")
    private Instant checkedAt;

    @Schema(description = "Ellenőrzött folyószámlák száma", example = "7")
    private long accountCount;

    @Schema(description = "Ellenőrzött megtakarítások száma", example = "3")
    private long savingsCount;

    @Schema(description = "Igaz, ha nincs eltérés", example = "true")
    private boolean balanced;

    @Schema(description = "Az eltérések (helyes működésnél üres)")
    private List<ReconciliationItemResponse> mismatches;
}
