package hu.finex.main.controller;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import hu.finex.main.dto.AccountResponse;
import hu.finex.main.dto.UpdateAccountStatusRequest;
import hu.finex.main.model.enums.AccountStatus;
import hu.finex.main.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/admin/accounts")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin – Account API", description = "Bankszámlák felügyelete (csak ADMIN)")
public class AdminAccountController {

    private final AccountService accountService;

    @GetMapping
    @Operation(summary = "Összes számla, opcionálisan státusz szerint (lapozva)",responses = {
            @ApiResponse(responseCode = "200", description = "Sikeres lekérdezés")
        }
    )
    public ResponseEntity<Page<AccountResponse>> listAll(@RequestParam(value = "status", required = false) AccountStatus status,
                                                         @ParameterObject @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(accountService.listAll(status, pageable));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Számla státuszának módosítása (befagyasztás, tiltás, feloldás, lezárás)",responses = {
            @ApiResponse(responseCode = "200", description = "Sikeres módosítás",
                content = @Content(schema = @Schema(implementation = AccountResponse.class))),
            @ApiResponse(responseCode = "400", description = "Érvénytelen állapotváltás"),
            @ApiResponse(responseCode = "404", description = "Számla nem található")
        }
    )
    public ResponseEntity<AccountResponse> updateStatus(@PathVariable("id") Long id,@Valid @RequestBody UpdateAccountStatusRequest request) {
        return ResponseEntity.ok(accountService.updateStatus(id, request));
    }
}
