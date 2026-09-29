package hu.finex.main.controller;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import hu.finex.main.dto.TransactionListItemResponse;
import hu.finex.main.dto.TransactionResponse;
import hu.finex.main.dto.TransactionSearchRequest;
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
@Tag(name = "Transaction API", description = "Tranzakciók keresése és utalás")
public class TransactionController {

    private final TransactionService transactionService;

    @GetMapping
    @Operation(summary = "Saját tranzakciók keresése (lapozható)", description = "Minden szűrő opcionális: számla, típus, időszak, összeg, szöveg (megjegyzés vagy partner), kategória. Alapértelmezés: a legújabb elöl.",responses = {
                @ApiResponse(responseCode = "200", description = "Sikeres lekérdezés"),
                @ApiResponse(responseCode = "404", description = "A megadott számla nem található")
            }
    )
    public ResponseEntity<Page<TransactionListItemResponse>> search(@ParameterObject TransactionSearchRequest filter,
                                                                    @ParameterObject @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(transactionService.search(filter, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Saját tranzakció lekérdezése ID alapján",responses = {
                @ApiResponse(responseCode = "200", description = "Sikeres lekérdezés",content = @Content(schema = @Schema(implementation = TransactionResponse.class))),
                @ApiResponse(responseCode = "404", description = "Tranzakció nem található")
            }
    )
    public ResponseEntity<TransactionResponse> getById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(transactionService.getById(id));
    }

    @PostMapping("/transfer")
    @Operation(summary = "Utalás IBAN számlaszámra",description = "A saját számláról utal. FineX-es címzettnél azonnal jóváíródik (TRANSFER_OUT + TRANSFER_IN), " +
                          "külső számlánál csak a terhelés történik meg. Ellenőrzi a fedezetet, a számlák állapotát, a devizanemet és a napi limitet.",responses = {
                    @ApiResponse(responseCode = "200", description = "Sikeres utalás",content = @Content(schema = @Schema(implementation = TransferResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Hibás kérés, nincs fedezet vagy túllépné a limitet"),
                    @ApiResponse(responseCode = "404", description = "Forrás számla nem található")
            }
    )
    public ResponseEntity<TransferResponse> transfer(@Valid @RequestBody TransferRequest request) {
        return ResponseEntity.ok(transactionService.transfer(request));
    }

}
