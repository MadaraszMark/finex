package hu.finex.main.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import hu.finex.main.dto.JobRunResponse;
import hu.finex.main.exception.BusinessException;
import hu.finex.main.exception.NotFoundException;
import hu.finex.main.service.StandingOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// Rendszeres átutalások: óránként fut, és teljesíti a ma vagy korábban esedékes megbízásokat

@Slf4j
@Component
@RequiredArgsConstructor
public class StandingOrderScheduler {

    private final StandingOrderService standingOrderService;

    @Scheduled(cron = "0 5 * * * *", zone = "Europe/Budapest")
    public void scheduledRun() {
        run();
    }

    // Megbízásonként külön tranzakció, a sikertelen teljesítést egy önálló tranzakció rögzíti
    public JobRunResponse run() {
        int processed = 0;
        int skipped = 0;

        for (Long orderId : standingOrderService.findDueOrderIds()) {
            try {
                standingOrderService.execute(orderId);
                processed++;
            } catch (BusinessException | NotFoundException e) {
                skipped++;
                standingOrderService.handleFailedExecution(orderId, e.getMessage());
            } catch (RuntimeException e) {
                skipped++;
                log.error("Rendszeres átutalás hiba (megbízás: {})", orderId, e);
            }
        }

        if (processed > 0 || skipped > 0) {
            log.info("Rendszeres átutalások: {} teljesítve, {} sikertelen", processed, skipped);
        }

        return JobRunResponse.builder()
                .job("Rendszeres átutalások")
                .processed(processed)
                .skipped(skipped)
                .build();
    }
}
