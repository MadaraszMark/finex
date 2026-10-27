package hu.finex.main.service;

import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hu.finex.main.dto.ReconciliationItemResponse;
import hu.finex.main.dto.ReconciliationResponse;
import hu.finex.main.repository.ReportRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReconciliationService {

    private final ReportRepository reportRepository;

    // Admin: főkönyvi egyeztetés egy tranzakcióban, hogy a lekérdezések ugyanazt az állapotot lássák
    @Transactional(readOnly = true)
    public ReconciliationResponse check() {
        List<ReconciliationItemResponse> mismatches = reportRepository.findReconciliationMismatches();

        return ReconciliationResponse.builder()
                .checkedAt(Instant.now())
                .accountCount(reportRepository.countReconciledAccounts())
                .savingsCount(reportRepository.countReconciledSavings())
                .balanced(mismatches.isEmpty())
                .mismatches(mismatches)
                .build();
    }
}
