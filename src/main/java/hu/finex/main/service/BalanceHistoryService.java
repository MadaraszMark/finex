package hu.finex.main.service;

import hu.finex.main.dto.BalanceHistoryListItemResponse;
import hu.finex.main.dto.BalanceHistoryResponse;
import hu.finex.main.exception.NotFoundException;
import hu.finex.main.mapper.BalanceHistoryMapper;
import hu.finex.main.model.BalanceHistory;
import hu.finex.main.repository.AccountRepository;
import hu.finex.main.repository.BalanceHistoryRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class BalanceHistoryService {

    private final BalanceHistoryRepository balanceHistoryRepository;
    private final AccountRepository accountRepository;
    private final BalanceHistoryMapper balanceHistoryMapper;

    @Transactional(readOnly = true)
    public BalanceHistoryResponse getById(Long id) {
        BalanceHistory history = balanceHistoryRepository.findById(id).orElseThrow(() -> new NotFoundException("Múltbéli egyenleg rekord nem található."));
        return balanceHistoryMapper.toResponse(history);
    }

    @Transactional(readOnly = true)
    public Page<BalanceHistoryListItemResponse> listByAccount(Long accountId, Pageable pageable) {
        accountRepository.findById(accountId).orElseThrow(() -> new NotFoundException("Számla nem található."));

        return balanceHistoryRepository.findByAccount_IdOrderByCreatedAtAsc(accountId, pageable).map(balanceHistoryMapper::toListItem);
    }

    @Transactional(readOnly = true)
    public Page<BalanceHistoryListItemResponse> listByAccountBetween(Long accountId,OffsetDateTime start,OffsetDateTime end,Pageable pageable) {
        accountRepository.findById(accountId).orElseThrow(() -> new NotFoundException("Számla nem található."));

        return balanceHistoryRepository.findByAccount_IdAndCreatedAtBetweenOrderByCreatedAtAsc(accountId, start, end, pageable).map(balanceHistoryMapper::toListItem);
    }
}
