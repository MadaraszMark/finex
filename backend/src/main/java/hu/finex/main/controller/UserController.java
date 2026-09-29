package hu.finex.main.controller;

import hu.finex.main.dto.ChangePasswordRequest;
import hu.finex.main.dto.UpdateUserRequest;
import hu.finex.main.dto.UserResponse;
import hu.finex.main.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "User API", description = "A bejelentkezett felhasználó saját adatai")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    @Operation(summary = "Bejelentkezett felhasználó adatainak lekérése",responses = {
                    @ApiResponse(responseCode = "200", description = "Sikeres lekérdezés",
                        content = @Content(schema = @Schema(implementation = UserResponse.class))),
                    @ApiResponse(responseCode = "401", description = "Nincs bejelentkezve")
            }
    )
    public ResponseEntity<UserResponse> getOwnProfile() {
        return ResponseEntity.ok(userService.getOwnProfile());
    }

    @PutMapping("/me")
    @Operation(summary = "Saját adatok módosítása",responses = {
                    @ApiResponse(responseCode = "200", description = "Sikeres módosítás",
                        content = @Content(schema = @Schema(implementation = UserResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Érvénytelen bemenet vagy az email már használatban van")
            }
    )
    public ResponseEntity<UserResponse> updateOwnProfile(@Valid @RequestBody UpdateUserRequest request) {
        return ResponseEntity.ok(userService.updateOwnProfile(request));
    }

    @PutMapping("/me/password")
    @Operation(summary = "Jelszócsere",responses = {
                    @ApiResponse(responseCode = "204", description = "Sikeres jelszócsere"),
                    @ApiResponse(responseCode = "400", description = "Hibás jelenlegi jelszó vagy gyenge új jelszó")
            }
    )
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(request);
        return ResponseEntity.noContent().build();
    }
}
