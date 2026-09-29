package hu.finex.main.scheduler;

import hu.finex.main.dto.JobRunResponse;
import hu.finex.main.exception.BusinessException;
import hu.finex.main.service.StandingOrderService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StandingOrderSchedulerTest {

    @Mock private StandingOrderService standingOrderService;

    @InjectMocks private StandingOrderScheduler scheduler;

    @Test
    void run_shouldExecuteEachDueOrder_andRecordFailures() {
        when(standingOrderService.findDueOrderIds()).thenReturn(List.of(1L, 2L, 3L));
        doNothing().when(standingOrderService).execute(1L);
        doThrow(new BusinessException("Nincs elegendő fedezet a számlán.")).when(standingOrderService).execute(2L);
        doNothing().when(standingOrderService).execute(3L);

        JobRunResponse result = scheduler.run();

        // Egy sikertelen megbízás nem akasztja meg a többit
        assertEquals(2, result.getProcessed());
        assertEquals(1, result.getSkipped());
        verify(standingOrderService).execute(1L);
        verify(standingOrderService).execute(3L);
        verify(standingOrderService).handleFailedExecution(2L, "Nincs elegendő fedezet a számlán.");
        verify(standingOrderService, never()).handleFailedExecution(eq(1L), any());
    }

    @Test
    void run_shouldCountUnexpectedErrorAsSkipped_withoutRecording() {
        when(standingOrderService.findDueOrderIds()).thenReturn(List.of(1L));
        doThrow(new IllegalStateException("adatbázis hiba")).when(standingOrderService).execute(1L);

        JobRunResponse result = scheduler.run();

        assertEquals(0, result.getProcessed());
        assertEquals(1, result.getSkipped());
        verify(standingOrderService, never()).handleFailedExecution(any(), any());
    }
}
