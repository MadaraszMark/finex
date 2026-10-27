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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import hu.finex.main.dto.AuditLogResponse;
import hu.finex.main.model.enums.AuditAction;
import hu.finex.main.service.AuditService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/admin/audit-log")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin – Audit Log API", description = "Az adminisztrátori műveletek naplója (csak ADMIN)")
public class AdminAuditLogController {

    private final AuditService auditService;

    @GetMapping
    @Operation(summary = "Adminműveletek keresése",description = "Minden szűrő opcionális: művelet, admin, időszak (napra pontosan). A legfrissebb elöl.",responses = {
                @ApiResponse(responseCode = "200", description = "Sikeres lekérdezés"),
                @ApiResponse(responseCode = "400", description = "Hibás időszak")
            }
    )
    public ResponseEntity<Page<AuditLogResponse>> search(@RequestParam(value = "action", required = false) AuditAction action,
                                                         @RequestParam(value = "actorId", required = false) Long actorId,
                                                         @RequestParam(value = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                                                         @RequestParam(value = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
                                                         @ParameterObject @PageableDefault(size = 50) Pageable pageable) {
        return ResponseEntity.ok(auditService.search(action, actorId, from, to, pageable));
    }
}
