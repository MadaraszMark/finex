package hu.finex.main.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hu.finex.main.dto.BalanceHistoryListItemResponse;
import hu.finex.main.exception.BusinessException;
import hu.finex.main.exception.NotFoundException;
import hu.finex.main.mapper.BalanceHistoryMapper;
import hu.finex.main.repository.AccountRepository;
import hu.finex.main.repository.BalanceHistoryRepository;
import hu.finex.main.security.CurrentUser;
import hu.finex.main.util.DateUtils;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BalanceHistoryService {

    private final BalanceHistoryRepository balanceHistoryRepository;
    private final AccountRepository accountRepository;
    private final BalanceHistoryMapper balanceHistoryMapper;
    private final CurrentUser currentUser;

    // Egy saját számla egyenlegének alakulása (grafikonhoz); alapértelmezés: az elmúlt 90 nap
    @Transactional(readOnly = true)
    public List<BalanceHistoryListItemResponse> listByAccount(Long accountId, LocalDate from, LocalDate to) {
        if (!accountRepository.existsByIdAndUser_Id(accountId, currentUser.requireId())) {
            throw new NotFoundException("Számla nem található.");
        }

        LocalDate end = to != null ? to : DateUtils.today();
        LocalDate start = from != null ? from : end.minusDays(90);

        if (start.isAfter(end)) {
            throw new BusinessException("A kezdő dátum nem lehet későbbi a záró dátumnál.");
        }

        return balanceHistoryRepository.findByAccount_IdAndCreatedAtBetweenOrderByCreatedAtAsc(accountId, DateUtils.startOfDay(start), DateUtils.startOfDay(end.plusDays(1)))
                .stream()
                .map(balanceHistoryMapper::toListItem)
                .toList();
    }
}
