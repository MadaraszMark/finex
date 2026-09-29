package hu.finex.main.dto;

import java.time.Instant;

import hu.finex.main.model.enums.UserRole;
import hu.finex.main.model.enums.UserStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Felhasználó egyszerűsített adatainak listázása")
public class UserListItemResponse {

    @Schema(description = "Felhasználó azonosítója", example = "42")
    private Long id;

    @Schema(description = "Felhasználó teljes neve", example = "Kovács Bence")
    private String fullName;

    @Schema(description = "Email cím", example = "bence.kovacs@example.com")
    private String email;

    @Schema(description = "Szerepkör", example = "ADMIN")
    private UserRole role;

    @Schema(description = "Státusz", example = "ACTIVE")
    private UserStatus status;

    @Schema(description = "Regisztráció időpontja", example = "2025-02-12T14:22:10Z")
    private Instant createdAt;
}

