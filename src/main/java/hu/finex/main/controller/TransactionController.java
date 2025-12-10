package hu.finex.main.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import hu.finex.main.dto.CreateTransactionRequest;
import hu.finex.main.dto.TransactionListItemResponse;
import hu.finex.main.dto.TransactionResponse;
import hu.finex.main.dto.TransferRequest;
import hu.finex.main.dto.TransferResponse;
import hu.finex.main.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
@Tag(name = "Transaction API", description = "Tranzakciók kezelése és lekérdezése")
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping
    @Operation(summary = "Új tranzakció létrehozása",responses = {
                @ApiResponse(responseCode = "200", description = "Sikeres létrehozás",content = @Content(schema = @Schema(implementation = TransactionResponse.class))),
                @ApiResponse(responseCode = "400", description = "Érvénytelen adatok"),
                @ApiResponse(responseCode = "404", description = "Számla nem található")
            }
    )
    public ResponseEntity<TransactionResponse> create(@Valid @RequestBody CreateTransactionRequest request) {
        return ResponseEntity.ok(transactionService.create(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Tranzakció lekérdezése ID alapján",responses = {
                @ApiResponse(responseCode = "200", description = "Sikeres lekérdezés",content = @Content(schema = @Schema(implementation = TransactionResponse.class))),
                @ApiResponse(responseCode = "404", description = "Tranzakció nem található")
            }
    )
    public ResponseEntity<TransactionResponse> getById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(transactionService.getById(id));
    }

    @GetMapping("/account/{accountId}")
    @Operation(summary = "Tranzakciók listázása adott számlához (lapozható)",responses = {
                @ApiResponse(responseCode = "200", description = "Sikeres lekérdezés"),
                @ApiResponse(responseCode = "404", description = "Számla nem található")
            }
    )
    public ResponseEntity<Page<TransactionListItemResponse>> listByAccount(@PathVariable("accountId") Long accountId,Pageable pageable) {
        return ResponseEntity.ok(transactionService.listByAccount(accountId, pageable));
    }
    
    @PostMapping("/transfer")
    @Operation(summary = "Pénz utalása két bankszámla között",description = "Ugyanabban a devizanemben tartott számlák között utal. " +
                          "Két tranzakció jön létre: TRANSFER_OUT és TRANSFER_IN.",responses = {
                    @ApiResponse(responseCode = "200", description = "Sikeres utalás",content = @Content(schema = @Schema(implementation = TransferResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Hibás kérés"),
                    @ApiResponse(responseCode = "404", description = "Számla nem található")
            }
    )
    public ResponseEntity<TransferResponse> transfer(@Valid @RequestBody TransferRequest request) {
        return ResponseEntity.ok(transactionService.transfer(request));
    }

}
