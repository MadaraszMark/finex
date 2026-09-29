package hu.finex.main.controller;

import hu.finex.main.dto.AuthResponse;
import hu.finex.main.dto.CreateUserRequest;
import hu.finex.main.dto.LoginRequest;
import hu.finex.main.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication",description = "Regisztráció és bejelentkezés kezelése (JWT alapú hitelesítés)")
public class AuthController {

    private final AuthService authService;

    //  Bejelentkezés

    @PostMapping("/login")
    @Operation(summary = "Bejelentkezés",description = "JWT token generálása érvényes email + jelszó megadásával. Minden próbálkozás naplózásra kerül, 5 sikertelen próbálkozás után a fiók 15 percre zárolódik.",responses = {
                    @ApiResponse(responseCode = "200",
                            description = "Sikeres bejelentkezés",
                            content = @Content(schema = @Schema(implementation = AuthResponse.class))
                    ),
                    @ApiResponse(responseCode = "400", description = "Hibás email vagy jelszó, letiltott vagy átmenetileg zárolt fiók")
            }
    )
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request,HttpServletRequest httpRequest) {
        String ip = httpRequest.getRemoteAddr();
        String userAgent = httpRequest.getHeader("User-Agent");

        AuthResponse response = authService.login(request, ip, userAgent);
        return ResponseEntity.ok(response);
    }

    // Regisztráció

    @PostMapping("/register")
    @Operation(summary = "Regisztráció",description = "Új felhasználó létrehozása (mindig USER szerepkörrel). Automatikusan létrejön egy forint folyószámla bankkártyával, és a válaszban már a bejelentkezési token is benne van.",responses = {
                    @ApiResponse(responseCode = "201",
                            description = "Sikeres regisztráció",
                            content = @Content(schema = @Schema(implementation = AuthResponse.class))
                    ),
                    @ApiResponse(responseCode = "400", description = "Hibás bemenet vagy az email már foglalt")
            }
    )
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody CreateUserRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
