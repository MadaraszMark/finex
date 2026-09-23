package hu.finex.main.service;

import hu.finex.main.dto.BalanceHistoryListItemResponse;
import hu.finex.main.dto.BalanceHistoryResponse;
import hu.finex.main.exception.NotFoundException;
import hu.finex.main.mapper.BalanceHistoryMapper;
import hu.finex.main.model.Account;
import hu.finex.main.model.BalanceHistory;
import hu.finex.main.repository.AccountRepository;
import hu.finex.main.repository.BalanceHistoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BalanceHistoryServiceTest {

    @Mock private BalanceHistoryRepository balanceHistoryRepository;
    @Mock private AccountRepository accountRepository;
    @Mock private BalanceHistoryMapper balanceHistoryMapper;

    @InjectMocks private BalanceHistoryService service;

    @Test
    void getById_shouldReturnResponse() {
        BalanceHistory history = BalanceHistory.builder().id(10L).build();
        when(balanceHistoryRepository.findById(10L)).thenReturn(Optional.of(history));

        BalanceHistoryResponse expected = BalanceHistoryResponse.builder().id(10L).build();
        when(balanceHistoryMapper.toResponse(history)).thenReturn(expected);

        BalanceHistoryResponse resp = service.getById(10L);

        assertNotNull(resp);
        assertEquals(10L, resp.getId());

        verify(balanceHistoryRepository).findById(10L);
        verify(balanceHistoryMapper).toResponse(history);
    }

    @Test
    void getById_shouldThrowNotFound_whenMissing() {
        when(balanceHistoryRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.getById(10L));

        verify(balanceHistoryRepository).findById(10L);
        verifyNoInteractions(balanceHistoryMapper);
    }

    @Test
    void listByAccount_shouldThrowNotFound_whenAccountMissing() {
        Pageable pageable = PageRequest.of(0, 10);
        when(accountRepository.findById(5L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.listByAccount(5L, pageable));

        verify(accountRepository).findById(5L);
        verifyNoInteractions(balanceHistoryRepository, balanceHistoryMapper);
    }

    @Test
    void listByAccount_shouldReturnMappedPage() {
        Pageable pageable = PageRequest.of(0, 2);

        when(accountRepository.findById(5L)).thenReturn(Optional.of(Account.builder().id(5L).build()));

        BalanceHistory h1 = BalanceHistory.builder().id(1L).build();
        BalanceHistory h2 = BalanceHistory.builder().id(2L).build();
        Page<BalanceHistory> page = new PageImpl<>(List.of(h1, h2), pageable, 2);

        when(balanceHistoryRepository.findByAccount_IdOrderByCreatedAtAsc(5L, pageable)).thenReturn(page);

        BalanceHistoryListItemResponse r1 = BalanceHistoryListItemResponse.builder().build();
        BalanceHistoryListItemResponse r2 = BalanceHistoryListItemResponse.builder().build();
        when(balanceHistoryMapper.toListItem(h1)).thenReturn(r1);
        when(balanceHistoryMapper.toListItem(h2)).thenReturn(r2);

        Page<BalanceHistoryListItemResponse> resp = service.listByAccount(5L, pageable);

        assertNotNull(resp);
        assertEquals(2, resp.getTotalElements());
        assertEquals(2, resp.getContent().size());
        assertSame(r1, resp.getContent().get(0));
        assertSame(r2, resp.getContent().get(1));

        verify(accountRepository).findById(5L);
        verify(balanceHistoryRepository).findByAccount_IdOrderByCreatedAtAsc(5L, pageable);
        verify(balanceHistoryMapper).toListItem(h1);
        verify(balanceHistoryMapper).toListItem(h2);
    }

    @Test
    void listByAccountBetween_shouldThrowNotFound_whenAccountMissing() {
        Pageable pageable = PageRequest.of(0, 10);
        OffsetDateTime start = OffsetDateTime.parse("2025-01-01T00:00:00+00:00");
        OffsetDateTime end = OffsetDateTime.parse("2025-02-01T00:00:00+00:00");

        when(accountRepository.findById(5L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.listByAccountBetween(5L, start, end, pageable));

        verify(accountRepository).findById(5L);
        verifyNoInteractions(balanceHistoryRepository, balanceHistoryMapper);
    }

    @Test
    void listByAccountBetween_shouldReturnMappedPage() {
        Pageable pageable = PageRequest.of(0, 2);
        OffsetDateTime start = OffsetDateTime.parse("2025-01-01T00:00:00+00:00");
        OffsetDateTime end = OffsetDateTime.parse("2025-02-01T00:00:00+00:00");

        when(accountRepository.findById(5L)).thenReturn(Optional.of(Account.builder().id(5L).build()));

        BalanceHistory h1 = BalanceHistory.builder().id(1L).build();
        BalanceHistory h2 = BalanceHistory.builder().id(2L).build();
        Page<BalanceHistory> page = new PageImpl<>(List.of(h1, h2), pageable, 2);

        when(balanceHistoryRepository.findByAccount_IdAndCreatedAtBetweenOrderByCreatedAtAsc(5L, start, end, pageable))
                .thenReturn(page);

        BalanceHistoryListItemResponse r1 = BalanceHistoryListItemResponse.builder().build();
        BalanceHistoryListItemResponse r2 = BalanceHistoryListItemResponse.builder().build();
        when(balanceHistoryMapper.toListItem(h1)).thenReturn(r1);
        when(balanceHistoryMapper.toListItem(h2)).thenReturn(r2);

        Page<BalanceHistoryListItemResponse> resp = service.listByAccountBetween(5L, start, end, pageable);

        assertNotNull(resp);
        assertEquals(2, resp.getTotalElements());
        assertEquals(2, resp.getContent().size());
        assertSame(r1, resp.getContent().get(0));
        assertSame(r2, resp.getContent().get(1));

        verify(accountRepository).findById(5L);
        verify(balanceHistoryRepository).findByAccount_IdAndCreatedAtBetweenOrderByCreatedAtAsc(5L, start, end, pageable);
        verify(balanceHistoryMapper).toListItem(h1);
        verify(balanceHistoryMapper).toListItem(h2);
    }
}
