package hu.finex.main.service;

import hu.finex.main.dto.BalanceHistoryListItemResponse;
import hu.finex.main.exception.BusinessException;
import hu.finex.main.exception.NotFoundException;
import hu.finex.main.mapper.BalanceHistoryMapper;
import hu.finex.main.model.BalanceHistory;
import hu.finex.main.repository.AccountRepository;
import hu.finex.main.repository.BalanceHistoryRepository;
import hu.finex.main.security.CurrentUser;
import hu.finex.main.util.DateUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BalanceHistoryServiceTest {

    @Mock private BalanceHistoryRepository balanceHistoryRepository;
    @Mock private AccountRepository accountRepository;
    @Mock private BalanceHistoryMapper balanceHistoryMapper;
    @Mock private CurrentUser currentUser;

    @InjectMocks private BalanceHistoryService service;

    @Test
    void listByAccount_shouldThrowNotFound_whenAccountIsNotOwn() {
        when(currentUser.requireId()).thenReturn(7L);
        when(accountRepository.existsByIdAndUser_Id(1L, 7L)).thenReturn(false);

        assertThrows(NotFoundException.class, () -> service.listByAccount(1L, null, null));

        verifyNoInteractions(balanceHistoryRepository, balanceHistoryMapper);
    }

    @Test
    void listByAccount_shouldReturnMappedItems_forGivenPeriod() {
        when(currentUser.requireId()).thenReturn(7L);
        when(accountRepository.existsByIdAndUser_Id(1L, 7L)).thenReturn(true);

        LocalDate from = LocalDate.of(2025, 3, 1);
        LocalDate to = LocalDate.of(2025, 3, 31);

        BalanceHistory h1 = BalanceHistory.builder().id(1L).balance(new BigDecimal("100.00")).build();
        BalanceHistory h2 = BalanceHistory.builder().id(2L).balance(new BigDecimal("80.00")).build();

        // A záró nap is benne van: a felső határ a következő nap kezdete
        when(balanceHistoryRepository.findByAccount_IdAndCreatedAtBetweenOrderByCreatedAtAsc(1L, DateUtils.startOfDay(from), DateUtils.startOfDay(LocalDate.of(2025, 4, 1))))
                .thenReturn(List.of(h1, h2));

        BalanceHistoryListItemResponse r1 = BalanceHistoryListItemResponse.builder().balance(new BigDecimal("100.00")).build();
        BalanceHistoryListItemResponse r2 = BalanceHistoryListItemResponse.builder().balance(new BigDecimal("80.00")).build();
        when(balanceHistoryMapper.toListItem(h1)).thenReturn(r1);
        when(balanceHistoryMapper.toListItem(h2)).thenReturn(r2);

        List<BalanceHistoryListItemResponse> out = service.listByAccount(1L, from, to);

        assertEquals(List.of(r1, r2), out);
    }

    @Test
    void listByAccount_shouldDefaultToLast90Days() {
        when(currentUser.requireId()).thenReturn(7L);
        when(accountRepository.existsByIdAndUser_Id(1L, 7L)).thenReturn(true);

        LocalDate today = DateUtils.today();
        when(balanceHistoryRepository.findByAccount_IdAndCreatedAtBetweenOrderByCreatedAtAsc(1L, DateUtils.startOfDay(today.minusDays(90)), DateUtils.startOfDay(today.plusDays(1))))
                .thenReturn(List.of());

        List<BalanceHistoryListItemResponse> out = service.listByAccount(1L, null, null);

        assertTrue(out.isEmpty());
    }

    @Test
    void listByAccount_shouldThrowBusinessException_whenFromIsAfterTo() {
        when(currentUser.requireId()).thenReturn(7L);
        when(accountRepository.existsByIdAndUser_Id(1L, 7L)).thenReturn(true);

        assertThrows(BusinessException.class, () -> service.listByAccount(1L, LocalDate.of(2025, 4, 1), LocalDate.of(2025, 3, 1)));

        verifyNoInteractions(balanceHistoryRepository);
    }
}
