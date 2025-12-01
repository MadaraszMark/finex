package hu.finex.main.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Felhasználói adatok módosításához szükséges adatok")
public class UpdateUserRequest {

    @NotBlank
    @Size(max = 100)
    @Schema(description = "Keresztnév", example = "Bence")
    private String firstName;

    @NotBlank
    @Size(max = 100)
    @Schema(description = "Vezetéknév", example = "Kovács")
    private String lastName;

    @Email
    @NotBlank
    @Size(max = 255)
    @Schema(description = "Email cím", example = "bence.kovacs@example.com")
    private String email;

    @Size(max = 30)
    @Schema(description = "Telefonszám", example = "+36301234567")
    private String phone;
}
