package hu.finex.main.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import hu.finex.main.service.IdempotencyService;
import hu.finex.main.service.RevokedTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// Óránként törli a lejárt visszavont tokeneket és a 7 napnál régebbi idempotencia-kulcsokat

@Slf4j
@Component
@RequiredArgsConstructor
public class CleanupScheduler {

    private final RevokedTokenService revokedTokenService;
    private final IdempotencyService idempotencyService;

    @Scheduled(cron = "0 25 * * * *", zone = "Europe/Budapest")
    public void scheduledRun() {
        int tokens = revokedTokenService.deleteExpired();
        int keys = idempotencyService.deleteExpired();
        if (tokens > 0 || keys > 0) {
            log.info("Karbantartás: {} lejárt visszavont token és {} régi idempotencia-kulcs törölve", tokens, keys);
        }
    }
}
