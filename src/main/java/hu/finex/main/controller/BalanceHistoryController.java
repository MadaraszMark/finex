package hu.finex.main.controller;

import java.time.OffsetDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import hu.finex.main.dto.BalanceHistoryListItemResponse;
import hu.finex.main.dto.BalanceHistoryResponse;
import hu.finex.main.service.BalanceHistoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/balance-history")
@RequiredArgsConstructor
@Tag(name = "Balance History API", description = "Számla egyenlegváltozások lekérése")
public class BalanceHistoryController {

    private final BalanceHistoryService balanceHistoryService;

    @GetMapping("/{id}")
    @Operation(summary = "Balance history rekord lekérdezése ID alapján",responses = {
                    @ApiResponse(responseCode = "200", description = "Sikeres lekérdezés",content = @Content(schema = @Schema(implementation = BalanceHistoryResponse.class))),
                    @ApiResponse(responseCode = "404", description = "Rekord nem található")
            }
    )
    public ResponseEntity<BalanceHistoryResponse> getById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(balanceHistoryService.getById(id));
    }

    @GetMapping("/account/{accountId}")
    @Operation(summary = "Egy adott számla összes balance history rekordja (lapozva)",description = "Időrendben növekvő sorrendben kapod vissza (legkorábbi → legújabb).",responses = {
                    @ApiResponse(responseCode = "200", description = "Sikeres lekérdezés",content = @Content(schema = @Schema(implementation = Page.class))),
                    @ApiResponse(responseCode = "404", description = "Számla nem található")
            }
    )
    public ResponseEntity<Page<BalanceHistoryListItemResponse>> listByAccount(@PathVariable("accountId") Long accountId,Pageable pageable) {
        return ResponseEntity.ok(balanceHistoryService.listByAccount(accountId, pageable)
        );
    }

    @GetMapping("/account/{accountId}/between")
    @Operation(summary = "Balance history időintervallum alapján",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Sikeres lekérdezés",content = @Content(schema = @Schema(implementation = Page.class))),
                    @ApiResponse(responseCode = "404", description = "Számla nem található"),
                    @ApiResponse(responseCode = "400", description = "Hibás időintervallum")
            }
    )
    public ResponseEntity<Page<BalanceHistoryListItemResponse>> listByAccountBetween(@PathVariable("accountId") Long accountId,@RequestParam OffsetDateTime start,@RequestParam OffsetDateTime end,Pageable pageable) {
        if (start.isAfter(end)) {
            throw new IllegalArgumentException("A kezdő időpont nem lehet később, mint a vég időpont.");
        }
        
        return ResponseEntity.ok(
                balanceHistoryService.listByAccountBetween(accountId, start, end, pageable)
        );
    }
}
