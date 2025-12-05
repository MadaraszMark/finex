package hu.finex.main.controller;

import hu.finex.main.dto.CreateSavingsAccountRequest;
import hu.finex.main.dto.SavingsAccountResponse;
import hu.finex.main.dto.UpdateSavingsAccountRequest;
import hu.finex.main.model.enums.SavingsStatus;
import hu.finex.main.service.SavingsAccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/savings")
@RequiredArgsConstructor
@Tag(name = "Savings Account API", description = "Megtakarítási számlák kezelése")
public class SavingsAccountController {

    private final SavingsAccountService savingsAccountService;

    @PostMapping
    @Operation(summary = "Új megtakarítási számla létrehozása",responses = {
                    @ApiResponse(responseCode = "200", description = "Sikeres létrehozás",content = @Content(schema = @Schema(implementation = SavingsAccountResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Hibás bemenet")
            }
    )
    public ResponseEntity<SavingsAccountResponse> create(@Valid @RequestBody CreateSavingsAccountRequest request) {
        return ResponseEntity.ok(savingsAccountService.create(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Megtakarítás lekérdezése ID alapján",responses = {
                    @ApiResponse(responseCode = "200", description = "Sikeres lekérdezés",content = @Content(schema = @Schema(implementation = SavingsAccountResponse.class))),
                    @ApiResponse(responseCode = "404", description = "Nem található")
            }
    )
    public ResponseEntity<SavingsAccountResponse> getById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(savingsAccountService.getById(id));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Felhasználó összes megtakarítása", responses = {
                    @ApiResponse(responseCode = "200", description = "Sikeres lekérdezés")
            }
    )
    public ResponseEntity<Page<SavingsAccountResponse>> listByUser(@PathVariable("userId") Long userId,Pageable pageable) {
        return ResponseEntity.ok(savingsAccountService.listByUser(userId, pageable));
    }

    @GetMapping("/user/{userId}/status/{status}")
    @Operation(summary = "Felhasználó megtakarításai státusz alapján",responses = {@ApiResponse(responseCode = "200", description = "Sikeres lekérdezés")})
    public ResponseEntity<Page<SavingsAccountResponse>> listByUserAndStatus(@PathVariable("userId") Long userId,@PathVariable SavingsStatus status,Pageable pageable) {
        return ResponseEntity.ok(savingsAccountService.listByUserAndStatus(userId, status, pageable));
    }

    @GetMapping("/user/{userId}/min-balance/{minBalance}")
    @Operation(summary = "Felhasználó megtakarításai minimum egyenleg alapján",responses = {@ApiResponse(responseCode = "200", description = "Sikeres lekérdezés")})
    public ResponseEntity<Page<SavingsAccountResponse>> listAboveBalance(@PathVariable("userId") Long userId,@PathVariable String minBalance,Pageable pageable) {
        return ResponseEntity.ok(
                savingsAccountService.listAboveBalance(userId, new java.math.BigDecimal(minBalance), pageable)
        );
    }

    @PutMapping("/{id}")
    @Operation(summary = "Megtakarítás adatainak módosítása",responses = {
                    @ApiResponse(responseCode = "200", description = "Sikeres módosítás",content = @Content(schema = @Schema(implementation = SavingsAccountResponse.class))),
                    @ApiResponse(responseCode = "404", description = "Nem található"),
                    @ApiResponse(responseCode = "409", description = "Név már foglalt")
            }
    )
    public ResponseEntity<SavingsAccountResponse> update(@PathVariable("id") Long id,@Valid @RequestBody UpdateSavingsAccountRequest request) {
        return ResponseEntity.ok(savingsAccountService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Megtakarítás törlése (soft delete: státusz CLOSED)",responses = {
                    @ApiResponse(responseCode = "204", description = "Sikeres törlés"),
                    @ApiResponse(responseCode = "404", description = "Nem található")
            }
    )
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        savingsAccountService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
