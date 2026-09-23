package hu.finex.main.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Bejelentkezéshez szükséges adatok")
public class LoginRequest {

    @Email
    @NotBlank
    @Schema(description = "Felhasználó email címe", example = "bence.kovacs@example.com")
    private String email;

    @NotBlank
    @Schema(description = "Jelszó", example = "TitkosJelszo123")
    private String password;
}

