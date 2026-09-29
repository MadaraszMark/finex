package hu.finex.main.dto;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "A bank egészére vonatkozó összesítő (admin)")
public class AdminDashboardResponse {

    @Schema(description = "Felhasználók száma", example = "128")
    private long userCount;

    @Schema(description = "Letiltott felhasználók száma", example = "2")
    private long blockedUserCount;

    @Schema(description = "Nem lezárt számlák száma", example = "140")
    private long accountCount;

    @Schema(description = "Forintbetétek összesen", example = "184250000.00")
    private BigDecimal totalDepositsHuf;

    @Schema(description = "Mai tranzakciók száma", example = "57")
    private long transactionsToday;

    @Schema(description = "Mai forintforgalom (terhelések)", example = "1250000.00")
    private BigDecimal transactionVolumeTodayHuf;

    @Schema(description = "Le nem zárt ügyfélszolgálati ticketek", example = "4")
    private long openTicketCount;

    @Schema(description = "Sikertelen belépések az elmúlt 24 órában", example = "6")
    private long failedLoginsLast24h;
}
