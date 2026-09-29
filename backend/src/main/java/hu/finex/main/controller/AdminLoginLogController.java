package hu.finex.main.controller;

import java.time.LocalDate;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
@RequestMapping("/admin/login-logs")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin – Login Log API", description = "Bejelentkezési napló biztonsági vizsgálathoz (csak ADMIN)")
public class AdminLoginLogController {

    private final LoginLogService loginLogService;

    @GetMapping
    @Operation(summary = "Bejelentkezési események keresése",description = "Minden szűrő opcionális: státusz, felhasználó, IP cím, időszak (napra pontosan).",responses = {
                @ApiResponse(responseCode = "200", description = "Sikeres lekérdezés"),
                @ApiResponse(responseCode = "400", description = "Hibás időszak")
            }
    )
    public ResponseEntity<Page<LoginLogResponse>> search(@RequestParam(value = "status", required = false) LoginStatus status,
                                                         @RequestParam(value = "userId", required = false) Long userId,
                                                         @RequestParam(value = "ipAddress", required = false) String ipAddress,
                                                         @RequestParam(value = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                                                         @RequestParam(value = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
                                                         @ParameterObject @PageableDefault(size = 50) Pageable pageable) {
        return ResponseEntity.ok(loginLogService.search(status, userId, ipAddress, from, to, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Login log lekérdezése ID alapján",responses = {
                @ApiResponse(responseCode = "200", description = "Sikeres lekérdezés",content = @Content(schema = @Schema(implementation = LoginLogResponse.class))),
                @ApiResponse(responseCode = "404", description = "Log nem található")
            }
    )
    public ResponseEntity<LoginLogResponse> getById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(loginLogService.getById(id));
    }
}
