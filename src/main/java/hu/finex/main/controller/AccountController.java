package hu.finex.main.controller;

import hu.finex.main.dto.*;
import hu.finex.main.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
@Tag(name = "Account API", description = "Bankszámlák kezelése")
public class AccountController {

    private final AccountService accountService;

    @PostMapping
    @Operation(summary = "Új bankszámla létrehozása",responses = {
            @ApiResponse(responseCode = "201", description = "Sikeresen létrehozva",
                content = @Content(schema = @Schema(implementation = AccountResponse.class))),
            @ApiResponse(responseCode = "400", description = "Érvénytelen bemenet"),
            @ApiResponse(responseCode = "404", description = "Felhasználó nem található")
        }
    )
    public ResponseEntity<AccountResponse> create(@Valid @RequestBody CreateAccountRequest request) {
        AccountResponse response = accountService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Bankszámla lekérdezése ID alapján",responses = {
            @ApiResponse(responseCode = "200", description = "Sikeres lekérdezés",
                content = @Content(schema = @Schema(implementation = AccountResponse.class))),
            @ApiResponse(responseCode = "404", description = "Számla nem található")
        }
    )
    public ResponseEntity<AccountResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(accountService.getById(id));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Felhasználó összes bankszámlájának lekérdezése",responses = {
            @ApiResponse(responseCode = "200", description = "Sikeres lekérdezés",
                content = @Content(schema = @Schema(implementation = AccountListItemResponse.class))),
            @ApiResponse(responseCode = "404", description = "Felhasználó nem található")
        }
    )
    public ResponseEntity<List<AccountListItemResponse>> listByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(accountService.listByUser(userId));
    }

    @PutMapping("/{id}/card")
    @Operation(summary = "Bankszámla kártyaszám módosítása",responses = {
            @ApiResponse(responseCode = "200", description = "Sikeres módosítás",
                content = @Content(schema = @Schema(implementation = AccountResponse.class))),
            @ApiResponse(responseCode = "400", description = "Érvénytelen bemenet"),
            @ApiResponse(responseCode = "404", description = "Számla nem található")
        }
    )
    public ResponseEntity<AccountResponse> updateCard(@PathVariable Long id,@Valid @RequestBody UpdateCardNumberRequest request) {

        return ResponseEntity.ok(accountService.updateCardNumber(id, request));
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Bankszámla státusz módosítása",responses = {
            @ApiResponse(responseCode = "200", description = "Sikeres módosítás",
                content = @Content(schema = @Schema(implementation = AccountResponse.class))),
            @ApiResponse(responseCode = "400", description = "Érvénytelen bemenet"),
            @ApiResponse(responseCode = "404", description = "Számla nem található")
        }
    )
    public ResponseEntity<AccountResponse> updateStatus(@PathVariable Long id,@Valid @RequestBody UpdateAccountStatusRequest request) {

        return ResponseEntity.ok(accountService.updateStatus(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Bankszámla lezárása (CLOSED státusz)",responses = {
            @ApiResponse(responseCode = "204", description = "Sikeres törlés"),
            @ApiResponse(responseCode = "404", description = "Számla nem található")
        }
    )
    
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        accountService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
