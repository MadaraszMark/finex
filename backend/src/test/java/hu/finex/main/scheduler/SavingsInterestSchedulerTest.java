package hu.finex.main.scheduler;

import hu.finex.main.dto.JobRunResponse;
import hu.finex.main.service.SavingsAccountService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SavingsInterestSchedulerTest {

    @Mock private SavingsAccountService savingsAccountService;

    @InjectMocks private SavingsInterestScheduler scheduler;

    @Test
    void run_shouldCreditEachSavings_andCountSkipped() {
        when(savingsAccountService.findIdsForInterest()).thenReturn(List.of(1L, 2L, 3L));
        when(savingsAccountService.creditMonthlyInterest(1L)).thenReturn(true);
        // 2: ebben a hónapban már megkapta a kamatot
        when(savingsAccountService.creditMonthlyInterest(2L)).thenReturn(false);
        when(savingsAccountService.creditMonthlyInterest(3L)).thenThrow(new IllegalStateException("zárolási hiba"));

        JobRunResponse result = scheduler.run();

        assertEquals("Havi kamatjóváírás", result.getJob());
        assertEquals(1, result.getProcessed());
        assertEquals(2, result.getSkipped());
    }

    @Test
    void run_shouldBeIdempotent_whenNothingIsDue() {
        when(savingsAccountService.findIdsForInterest()).thenReturn(List.of());

        JobRunResponse result = scheduler.run();

        assertEquals(0, result.getProcessed());
        assertEquals(0, result.getSkipped());
        verify(savingsAccountService, never()).creditMonthlyInterest(any());
    }
}
