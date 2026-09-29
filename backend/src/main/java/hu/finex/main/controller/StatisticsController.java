package hu.finex.main.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import hu.finex.main.dto.CategorySpendingResponse;
import hu.finex.main.dto.DashboardSummaryResponse;
import hu.finex.main.dto.MonthlySummaryResponse;
import hu.finex.main.service.StatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/statistics")
@RequiredArgsConstructor
@Tag(name = "Statistics API", description = "Főoldali összesítő és grafikonadatok")
public class StatisticsController {

    private final StatisticsService statisticsService;

    @GetMapping("/summary")
    @Operation(summary = "A főoldal összesítője (egyenlegek, e havi bevétel/kiadás, olvasatlan értesítések)",responses = {
                    @ApiResponse(responseCode = "200", description = "Sikeres lekérdezés",content = @Content(schema = @Schema(implementation = DashboardSummaryResponse.class)))
            }
    )
    public ResponseEntity<DashboardSummaryResponse> getSummary(@RequestParam(value = "currency", defaultValue = "HUF") String currency) {
        return ResponseEntity.ok(statisticsService.getSummary(currency));
    }

    @GetMapping("/monthly")
    @Operation(summary = "Havi bevétel és kiadás az elmúlt N hónapra (oszlopdiagramhoz)", description = "Az adatbázis v_account_monthly_summary nézetéből. Számla megadása nélkül az adott devizájú összes számlát összesíti.",responses = {
                    @ApiResponse(responseCode = "200", description = "Sikeres lekérdezés"),
                    @ApiResponse(responseCode = "400", description = "Hibás paraméter"),
                    @ApiResponse(responseCode = "404", description = "Számla nem található")
            }
    )
    public ResponseEntity<List<MonthlySummaryResponse>> getMonthlySummary(@RequestParam(value = "months", defaultValue = "6") int months,
                                                                          @RequestParam(value = "currency", defaultValue = "HUF") String currency,
                                                                          @RequestParam(value = "accountId", required = false) Long accountId) {
        return ResponseEntity.ok(statisticsService.getMonthlySummary(months, currency, accountId));
    }

    @GetMapping("/categories")
    @Operation(summary = "Költések kategóriánként (kördiagramhoz)", description = "Alapértelmezés: az aktuális hónap.",responses = {
                    @ApiResponse(responseCode = "200", description = "Sikeres lekérdezés"),
                    @ApiResponse(responseCode = "400", description = "Hibás időszak")
            }
    )
    public ResponseEntity<List<CategorySpendingResponse>> getCategorySpending(@RequestParam(value = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                                                                              @RequestParam(value = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
                                                                              @RequestParam(value = "currency", defaultValue = "HUF") String currency) {
        return ResponseEntity.ok(statisticsService.getCategorySpending(from, to, currency));
    }
}
