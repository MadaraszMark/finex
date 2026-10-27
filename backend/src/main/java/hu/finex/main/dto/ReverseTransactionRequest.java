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
@Schema(description = "Tétel sztornózása (admin)")
public class ReverseTransactionRequest {

    @NotBlank
    @Size(max = 200, message = "Az indoklás legfeljebb 200 karakter lehet.")
    @Schema(description = "A sztornó oka (a javító tétel közleményébe és az audit naplóba kerül)", example = "Kétszer terhelt kártyás fizetés", required = true)
    private String reason;
}
