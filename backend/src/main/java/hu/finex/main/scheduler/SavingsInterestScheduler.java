package hu.finex.main.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import hu.finex.main.dto.JobRunResponse;
import hu.finex.main.service.SavingsAccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// Havi kamatjóváírás a megtakarításokon. Óránként fut, de megtakarításonként havonta csak egyszer ír jóvá,
// így ha a szerver épp nem futott a hónap elején, a következő indulás után pótolja.

@Slf4j
@Component
@RequiredArgsConstructor
public class SavingsInterestScheduler {

    private final SavingsAccountService savingsAccountService;

    @Scheduled(cron = "0 15 * * * *", zone = "Europe/Budapest")
    public void scheduledRun() {
        run();
    }

    // Megtakarításonként külön tranzakció: egy hiba nem akasztja meg a többit (az admin kézzel is elindíthatja)
    public JobRunResponse run() {
        int processed = 0;
        int skipped = 0;

        for (Long savingsId : savingsAccountService.findIdsForInterest()) {
            try {
                if (savingsAccountService.creditMonthlyInterest(savingsId)) {
                    processed++;
                } else {
                    skipped++;
                }
            } catch (RuntimeException e) {
                skipped++;
                log.warn("Kamatjóváírás sikertelen (megtakarítás: {}): {}", savingsId, e.getMessage());
            }
        }

        if (processed > 0) {
            log.info("Havi kamatjóváírás: {} jóváírva, {} kihagyva", processed, skipped);
        }

        return JobRunResponse.builder()
                .job("Havi kamatjóváírás")
                .processed(processed)
                .skipped(skipped)
                .build();
    }
}
