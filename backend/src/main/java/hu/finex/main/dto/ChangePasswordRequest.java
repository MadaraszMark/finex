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
@Schema(description = "Jelszócsere adatai")
public class ChangePasswordRequest {

    @NotBlank
    @Schema(description = "A jelenlegi jelszó", example = "TitkosJelszo123", required = true)
    private String currentPassword;

    @NotBlank
    @Size(min = 8, max = 100, message = "A jelszó legalább 8 karakter legyen.")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$", message = "A jelszóban betűnek és számnak is lennie kell.")
    @Schema(description = "Az új jelszó (legalább 8 karakter, betű és szám)", example = "UjJelszo2025", required = true)
    private String newPassword;
}
