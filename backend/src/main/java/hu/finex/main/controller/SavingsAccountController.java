package hu.finex.main.controller;

import java.util.List;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import hu.finex.main.dto.CreateSavingsAccountRequest;
import hu.finex.main.dto.SavingsAccountResponse;
import hu.finex.main.dto.SavingsTransactionResponse;
import hu.finex.main.dto.SavingsTransferRequest;
import hu.finex.main.dto.SavingsTransferResponse;
import hu.finex.main.dto.UpdateSavingsAccountRequest;
import hu.finex.main.service.SavingsAccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/savings")
@RequiredArgsConstructor
@Tag(name = "Savings Account API", description = "A bejelentkezett felhasználó megtakarításai")
public class SavingsAccountController {

    private final SavingsAccountService savingsAccountService;

    @GetMapping
    @Operation(summary = "Saját (nem lezárt) megtakarítások", responses = {@ApiResponse(responseCode = "200", description = "Sikeres lekérdezés")}
    )
    public ResponseEntity<List<SavingsAccountResponse>> listMine() {
        return ResponseEntity.ok(savingsAccountService.listMine());
    }

    @PostMapping
    @Operation(summary = "Új megtakarítás létrehozása", description = "A kamatlábat a bank határozza meg; a kezdő összeg a megadott folyószámláról érkezik.",responses = {
                    @ApiResponse(responseCode = "201", description = "Sikeres létrehozás",content = @Content(schema = @Schema(implementation = SavingsAccountResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Hibás bemenet, foglalt név vagy nincs fedezet")
            }
    )
    public ResponseEntity<SavingsAccountResponse> create(@Valid @RequestBody CreateSavingsAccountRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(savingsAccountService.create(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Saját megtakarítás lekérdezése ID alapján",responses = {
                    @ApiResponse(responseCode = "200", description = "Sikeres lekérdezés",content = @Content(schema = @Schema(implementation = SavingsAccountResponse.class))),
                    @ApiResponse(responseCode = "404", description = "Nem található")
            }
    )
    public ResponseEntity<SavingsAccountResponse> getById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(savingsAccountService.getById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Megtakarítás átnevezése és célösszeg módosítása",responses = {
                    @ApiResponse(responseCode = "200", description = "Sikeres módosítás",content = @Content(schema = @Schema(implementation = SavingsAccountResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Hibás bemenet vagy foglalt név"),
                    @ApiResponse(responseCode = "404", description = "Nem található")
            }
    )
    public ResponseEntity<SavingsAccountResponse> update(@PathVariable("id") Long id,@Valid @RequestBody UpdateSavingsAccountRequest request) {
        return ResponseEntity.ok(savingsAccountService.update(id, request));
    }

    @PostMapping("/{id}/deposit-from-account")
    @Operation(summary = "Pénz átvezetése folyószámláról megtakarítási számlára",responses = {@ApiResponse(responseCode = "200", description = "Sikeres átvezetés",content = @Content(schema = @Schema(implementation = SavingsTransferResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Üzleti hiba (pl. nincs fedezet, nem aktív számla)"),
                    @ApiResponse(responseCode = "404", description = "Számla nem található")
            }
    )
    public ResponseEntity<SavingsTransferResponse> depositFromAccount(@PathVariable("id") Long savingsId,@Valid @RequestBody SavingsTransferRequest request) {
        return ResponseEntity.ok(
                savingsAccountService.depositFromAccount(savingsId, request)
        );
    }

    @PostMapping("/{id}/withdraw-to-account")
    @Operation(summary = "Pénz kivétele megtakarítási számláról folyószámlára",responses = {@ApiResponse(responseCode = "200", description = "Sikeres átvezetés",content = @Content(schema = @Schema(implementation = SavingsTransferResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Üzleti hiba (pl. nincs fedezet, nem aktív számla)"),
                    @ApiResponse(responseCode = "404", description = "Számla nem található")
            }
    )
    public ResponseEntity<SavingsTransferResponse> withdrawToAccount(@PathVariable("id") Long savingsId,@Valid @RequestBody SavingsTransferRequest request) {
        return ResponseEntity.ok(
                savingsAccountService.withdrawToAccount(savingsId, request)
        );
    }

    @GetMapping("/{id}/transactions")
    @Operation(summary = "A megtakarítás mozgásai (befizetés, kivét, kamat), lapozva",responses = {
                    @ApiResponse(responseCode = "200", description = "Sikeres lekérdezés"),
                    @ApiResponse(responseCode = "404", description = "Nem található")
            }
    )
    public ResponseEntity<Page<SavingsTransactionResponse>> listTransactions(@PathVariable("id") Long id,@ParameterObject @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(savingsAccountService.listTransactions(id, pageable));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Megtakarítás lezárása", description = "A teljes egyenleg visszakerül a megadott folyószámlára, a megtakarítás CLOSED státuszú lesz.",responses = {
                    @ApiResponse(responseCode = "204", description = "Sikeres lezárás"),
                    @ApiResponse(responseCode = "400", description = "Üzleti hiba"),
                    @ApiResponse(responseCode = "404", description = "Nem található")
            }
    )
    public ResponseEntity<Void> close(@PathVariable("id") Long id,@RequestParam("accountId") Long accountId) {
        savingsAccountService.close(id, accountId);
        return ResponseEntity.noContent().build();
    }
}
