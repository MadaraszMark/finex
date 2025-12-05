package hu.finex.main.controller;

import java.time.OffsetDateTime;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import hu.finex.main.dto.LoginLogResponse;
import hu.finex.main.model.enums.LoginStatus;
import hu.finex.main.service.LoginLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/login-logs")
@RequiredArgsConstructor
@Tag(name = "Login Log API", description = "Bejelentkezési események lekérdezése")
public class LoginLogController {

    private final LoginLogService loginLogService;

    @GetMapping("/{id}")
    @Operation(summary = "Login log lekérdezése ID alapján",responses = {
                @ApiResponse(responseCode = "200", description = "Sikeres lekérdezés",content = @Content(schema = @Schema(implementation = LoginLogResponse.class))),
                @ApiResponse(responseCode = "404", description = "Log nem található")
            }
    )
    public ResponseEntity<LoginLogResponse> getById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(loginLogService.getById(id));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Egy felhasználó összes login eseménye",responses = {
                @ApiResponse(responseCode = "200", description = "Sikeres lekérdezés"),
                @ApiResponse(responseCode = "404", description = "Felhasználó nem található")
            }
    )
    public ResponseEntity<?> listByUser(@PathVariable("userId") Long userId, Pageable pageable) {
        return ResponseEntity.ok(loginLogService.listByUser(userId, pageable));
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Login események szűrése státusz alapján (SUCCESS / FAILED)")
    public ResponseEntity<?> listByStatus(@PathVariable("status") LoginStatus status, Pageable pageable) {
        return ResponseEntity.ok(loginLogService.listByStatus(status, pageable));
    }

    @GetMapping("/ip/{ipAddress}")
    @Operation(summary = "Login események listázása IP cím alapján")
    public ResponseEntity<?> listByIp(@PathVariable("ipAddress") String ipAddress, Pageable pageable) {
        return ResponseEntity.ok(loginLogService.listByIp(ipAddress, pageable));
    }

    @GetMapping("/user/{userId}/status/{status}")
    @Operation(summary = "Felhasználó login eseményei megadott státusszal")
    public ResponseEntity<?> listByUserAndStatus(@PathVariable("userId") Long userId,@PathVariable LoginStatus status,Pageable pageable) {
        return ResponseEntity.ok(loginLogService.listByUserAndStatus(userId, status, pageable));
    }


    @GetMapping("/range")
    @Operation(summary = "Login események listázása időintervallum alapján",description = "Paraméterek: &start=2025-01-01T00:00:00Z&end=2025-02-01T00:00:00Z")
    public ResponseEntity<?> listByDateRange(@RequestParam OffsetDateTime start,@RequestParam OffsetDateTime end,Pageable pageable) {
        return ResponseEntity.ok(loginLogService.listByDateRange(start, end, pageable));
    }
}
