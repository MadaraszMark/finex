package hu.finex.main.service;

import hu.finex.main.dto.ReconciliationItemResponse;
import hu.finex.main.dto.ReconciliationResponse;
import hu.finex.main.repository.ReportRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReconciliationServiceTest {

    @Mock private ReportRepository reportRepository;

    @InjectMocks private ReconciliationService service;

    @Test
    void check_shouldBeBalanced_whenNoMismatch() {
        when(reportRepository.findReconciliationMismatches()).thenReturn(List.of());
        when(reportRepository.countReconciledAccounts()).thenReturn(7L);
        when(reportRepository.countReconciledSavings()).thenReturn(3L);

        ReconciliationResponse resp = service.check();

        assertTrue(resp.isBalanced());
        assertEquals(7L, resp.getAccountCount());
        assertEquals(3L, resp.getSavingsCount());
        assertNotNull(resp.getCheckedAt());
    }

    @Test
    void check_shouldListMismatches() {
        ReconciliationItemResponse item = ReconciliationItemResponse.builder().kind("ACCOUNT").id(4L).difference(new BigDecimal("200.00")).build();
        when(reportRepository.findReconciliationMismatches()).thenReturn(List.of(item));

        ReconciliationResponse resp = service.check();

        assertFalse(resp.isBalanced());
        assertEquals(List.of(item), resp.getMismatches());
    }
}
