package hu.finex.main.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import hu.finex.main.dto.BalanceHistoryListItemResponse;
import hu.finex.main.service.BalanceHistoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/balance-history")
@RequiredArgsConstructor
@Tag(name = "Balance History API", description = "Számla egyenlegváltozások lekérése")
public class BalanceHistoryController {

    private final BalanceHistoryService balanceHistoryService;

    @GetMapping("/account/{accountId}")
    @Operation(summary = "Egy saját számla egyenlegének alakulása (grafikonhoz)",description = "Időrendben növekvő sorrendben (legkorábbi → legújabb). Alapértelmezés: az elmúlt 90 nap.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Sikeres lekérdezés"),
                    @ApiResponse(responseCode = "400", description = "Hibás időintervallum"),
                    @ApiResponse(responseCode = "404", description = "Számla nem található")
            }
    )
    public ResponseEntity<List<BalanceHistoryListItemResponse>> listByAccount(@PathVariable("accountId") Long accountId,
                                                                             @RequestParam(value = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                                                                             @RequestParam(value = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(balanceHistoryService.listByAccount(accountId, from, to));
    }
}
