package hu.finex.main.controller;

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
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "User API", description = "Felhasználók adatainak kezelése")
public class UserController {

    private final UserService userService;

    @GetMapping("/{id}")
    @Operation(summary = "Felhasználó lekérdezése ID alapján",responses = {
                    @ApiResponse(responseCode = "200", description = "Sikeres lekérdezés",
                        content = @Content(schema = @Schema(implementation = UserResponse.class))),
                    @ApiResponse(responseCode = "404", description = "Felhasználó nem található")
            }
    )
    public ResponseEntity<UserResponse> getById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(userService.getById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Felhasználó adatainak módosítása",responses = {
                    @ApiResponse(responseCode = "200", description = "Sikeres módosítás",
                        content = @Content(schema = @Schema(implementation = UserResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Érvénytelen bemenet"),
                    @ApiResponse(responseCode = "404", description = "Felhasználó nem található"),
                    @ApiResponse(responseCode = "409", description = "Email cím már használatban van")
            }
    )
    public ResponseEntity<UserResponse> update(@PathVariable("id") Long id,@Valid @RequestBody UpdateUserRequest request) {
        return ResponseEntity.ok(userService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Felhasználó törlése ID alapján",responses = {
                    @ApiResponse(responseCode = "204", description = "Sikeres törlés"),
                    @ApiResponse(responseCode = "404", description = "Felhasználó nem található")
            }
    )
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    @Operation(summary = "Bejelentkezett felhasználó adatainak lekérése",responses = {
                    @ApiResponse(responseCode = "200", description = "Sikeres lekérdezés",
                        content = @Content(schema = @Schema(implementation = UserResponse.class))),
                    @ApiResponse(responseCode = "401", description = "Nincs bejelentkezve")
            }
    )
    public ResponseEntity<UserResponse> getOwnProfile(Authentication auth) {
        String email = (String) auth.getPrincipal();
        return ResponseEntity.ok(userService.getOwnProfile(email));
    }
}
