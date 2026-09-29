package hu.finex.main.controller;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import hu.finex.main.dto.LoginLogListItemResponse;
import hu.finex.main.service.LoginLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/login-logs")
@RequiredArgsConstructor
@Tag(name = "Login Log API", description = "Saját bejelentkezési előzmények (a teljes napló az admin felületen: /admin/login-logs)")
public class LoginLogController {

    private final LoginLogService loginLogService;

    @GetMapping("/me")
    @Operation(summary = "Saját bejelentkezési előzmények (eszköz, IP, sikeres / sikertelen)",responses = {
                @ApiResponse(responseCode = "200", description = "Sikeres lekérdezés")
            }
    )
    public ResponseEntity<Page<LoginLogListItemResponse>> listMine(@ParameterObject @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(loginLogService.listMine(pageable));
    }
}
