package hu.finex.main.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import hu.finex.main.dto.AdminDashboardResponse;
import hu.finex.main.dto.JobRunResponse;
import hu.finex.main.scheduler.SavingsInterestScheduler;
import hu.finex.main.scheduler.StandingOrderScheduler;
import hu.finex.main.service.AdminDashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin – Dashboard API", description = "Banki összesítő és az ütemezett feladatok kézi indítása (csak ADMIN)")
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;
    private final SavingsInterestScheduler savingsInterestScheduler;
    private final StandingOrderScheduler standingOrderScheduler;

    @GetMapping("/dashboard")
    @Operation(summary = "A bank egészére vonatkozó mutatók",responses = {
                    @ApiResponse(responseCode = "200", description = "Sikeres lekérdezés",content = @Content(schema = @Schema(implementation = AdminDashboardResponse.class)))
            }
    )
    public ResponseEntity<AdminDashboardResponse> getDashboard() {
        return ResponseEntity.ok(adminDashboardService.getDashboard());
    }

    @PostMapping("/jobs/savings-interest")
    @Operation(summary = "Havi kamatjóváírás azonnali futtatása (megtakarításonként havonta csak egyszer ír jóvá)",responses = {
                    @ApiResponse(responseCode = "200", description = "Lefutott",content = @Content(schema = @Schema(implementation = JobRunResponse.class)))
            }
    )
    public ResponseEntity<JobRunResponse> runSavingsInterest() {
        return ResponseEntity.ok(savingsInterestScheduler.run());
    }

    @PostMapping("/jobs/standing-orders")
    @Operation(summary = "Az esedékes rendszeres átutalások azonnali teljesítése",responses = {
                    @ApiResponse(responseCode = "200", description = "Lefutott",content = @Content(schema = @Schema(implementation = JobRunResponse.class)))
            }
    )
    public ResponseEntity<JobRunResponse> runStandingOrders() {
        return ResponseEntity.ok(standingOrderScheduler.run());
    }
}
