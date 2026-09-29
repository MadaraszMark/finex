package hu.finex.main.dto;

import java.time.Instant;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "JWT token és felhasználói adatok")
public class AuthResponse {

    @Schema(description = "JWT access token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String token;

    @Schema(description = "A token lejárati ideje", example = "2025-02-12T15:30:00Z")
    private Instant expiresAt;

    @Schema(description = "Bejelentkezett felhasználó adatai")
    private UserResponse user;
}

