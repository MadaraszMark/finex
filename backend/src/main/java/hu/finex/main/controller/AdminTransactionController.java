package hu.finex.main.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import hu.finex.main.dto.ReconciliationResponse;
import hu.finex.main.dto.ReverseTransactionRequest;
import hu.finex.main.dto.TransactionResponse;
import hu.finex.main.service.ReconciliationService;
import hu.finex.main.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin – Ledger API", description = "Sztornó és főkönyvi egyeztetés (csak ADMIN)")
public class AdminTransactionController {

    private final TransactionService transactionService;
    private final ReconciliationService reconciliationService;

    @PostMapping("/transactions/{id}/reverse")
    @Operation(summary = "Tétel sztornózása",description = "Az eredeti tétel nem módosul: egy ellentétes irányú javító tétel semlegesíti. Sztornózható: kártyás fizetés, " +
                          "készpénzbefizetés és kimenő utalás (belső utalásnál és devizaváltásnál a pár másik fele is). Az érintettek értesítést kapnak, a művelet az audit naplóba kerül.",responses = {
                    @ApiResponse(responseCode = "200", description = "A javító (sztornó) tétel",content = @Content(schema = @Schema(implementation = TransactionResponse.class))),
                    @ApiResponse(responseCode = "400", description = "A tétel nem sztornózható, már sztornózták, vagy nincs fedezet a sztornóhoz"),
                    @ApiResponse(responseCode = "404", description = "Tranzakció nem található")
            }
    )
    public ResponseEntity<TransactionResponse> reverse(@PathVariable("id") Long id,@Valid @RequestBody ReverseTransactionRequest request) {
        return ResponseEntity.ok(transactionService.reverse(id, request));
    }

    @GetMapping("/reconciliation")
    @Operation(summary = "Főkönyvi egyeztetés",description = "Minden számla és megtakarítás tárolt egyenlegét összeveti a könyvelt tételekből számolttal (adatbázis-nézetek).",responses = {
                    @ApiResponse(responseCode = "200", description = "Az egyeztetés eredménye",content = @Content(schema = @Schema(implementation = ReconciliationResponse.class)))
            }
    )
    public ResponseEntity<ReconciliationResponse> reconcile() {
        return ResponseEntity.ok(reconciliationService.check());
    }
}
