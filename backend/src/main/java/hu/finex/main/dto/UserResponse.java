package hu.finex.main.dto;

import java.time.Instant;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Felhasználó részletes adatai")
public class UserResponse {

    @Schema(description = "Felhasználó azonosítója", example = "42")
    private Long id;

    @Schema(description = "Keresztnév", example = "Bence")
    private String firstName;

    @Schema(description = "Vezetéknév", example = "Kovács")
    private String lastName;

    @Schema(description = "Email cím", example = "bence.kovacs@example.com")
    private String email;

    @Schema(description = "Telefonszám", example = "+36301234567")
    private String phone;

    @Schema(description = "Szerepkör", example = "USER")
    private String role;

    @Schema(description = "Létrehozás időpontja", example = "2025-02-12T14:22:10Z")
    private Instant createdAt;

    @Schema(description = "Utoljára módosítva", example = "2025-02-13T11:01:45Z")
    private Instant updatedAt;
}
