package hu.finex.main.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Új felhasználó regisztrációjához szükséges adatok (a szerepkör mindig USER)")
public class CreateUserRequest {

    @NotBlank
    @Size(max = 100)
    @Schema(description = "Keresztnév", example = "Bence", required = true)
    private String firstName;

    @NotBlank
    @Size(max = 100)
    @Schema(description = "Vezetéknév", example = "Kovács", required = true)
    private String lastName;

    @Email
    @NotBlank
    @Size(max = 255)
    @Schema(description = "Email cím", example = "bence.kovacs@example.com", required = true)
    private String email;

    @Size(max = 30)
    @Schema(description = "Telefonszám", example = "+36301234567")
    private String phone;

    @NotBlank
    @Size(min = 8, max = 100, message = "A jelszó legalább 8 karakter legyen.")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$", message = "A jelszóban betűnek és számnak is lennie kell.")
    @Schema(description = "Jelszó (plaintext, backend hash-eli). Legalább 8 karakter, betű és szám.", example = "TitkosJelszo123", required = true)
    private String password;
}

