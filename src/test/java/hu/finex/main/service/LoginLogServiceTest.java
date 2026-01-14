package hu.finex.main.service;

import hu.finex.main.dto.LoginLogListItemResponse;
import hu.finex.main.dto.LoginLogResponse;
import hu.finex.main.exception.NotFoundException;
import hu.finex.main.mapper.LoginLogMapper;
import hu.finex.main.model.LoginLog;
import hu.finex.main.model.enums.LoginStatus;
import hu.finex.main.repository.LoginLogRepository;
import hu.finex.main.repository.UserRepository;
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
class LoginLogServiceTest {

    @Mock private LoginLogRepository loginLogRepository;
    @Mock private UserRepository userRepository;
    @Mock private LoginLogMapper loginLogMapper;

    @InjectMocks private LoginLogService service;

    @Test
    void listByUser_shouldThrowNotFound_whenUserMissing() {
        Pageable pageable = PageRequest.of(0, 10);
        when(userRepository.existsById(5L)).thenReturn(false);

        assertThrows(NotFoundException.class, () -> service.listByUser(5L, pageable));

        verify(userRepository).existsById(5L);
        verifyNoInteractions(loginLogRepository, loginLogMapper);
    }

    @Test
    void listByUser_shouldReturnMappedPage() {
        Pageable pageable = PageRequest.of(0, 2);
        when(userRepository.existsById(5L)).thenReturn(true);

        LoginLog l1 = LoginLog.builder().id(1L).build();
        LoginLog l2 = LoginLog.builder().id(2L).build();
        Page<LoginLog> page = new PageImpl<>(List.of(l1, l2), pageable, 2);

        when(loginLogRepository.findByUser_IdOrderByCreatedAtDesc(5L, pageable)).thenReturn(page);

        LoginLogListItemResponse r1 = LoginLogListItemResponse.builder().build();
        LoginLogListItemResponse r2 = LoginLogListItemResponse.builder().build();
        when(loginLogMapper.toListItem(l1)).thenReturn(r1);
        when(loginLogMapper.toListItem(l2)).thenReturn(r2);

        Page<LoginLogListItemResponse> resp = service.listByUser(5L, pageable);

        assertNotNull(resp);
        assertEquals(2, resp.getTotalElements());
        assertSame(r1, resp.getContent().get(0));
        assertSame(r2, resp.getContent().get(1));

        verify(userRepository).existsById(5L);
        verify(loginLogRepository).findByUser_IdOrderByCreatedAtDesc(5L, pageable);
        verify(loginLogMapper).toListItem(l1);
        verify(loginLogMapper).toListItem(l2);
    }

    @Test
    void listByStatus_shouldReturnMappedPage() {
        Pageable pageable = PageRequest.of(0, 1);

        LoginLog log = LoginLog.builder().id(1L).status(LoginStatus.FAILED).build();
        Page<LoginLog> page = new PageImpl<>(List.of(log), pageable, 1);

        when(loginLogRepository.findByStatusOrderByCreatedAtDesc(LoginStatus.FAILED, pageable)).thenReturn(page);

        LoginLogListItemResponse item = LoginLogListItemResponse.builder().build();
        when(loginLogMapper.toListItem(log)).thenReturn(item);

        Page<LoginLogListItemResponse> resp = service.listByStatus(LoginStatus.FAILED, pageable);

        assertEquals(1, resp.getTotalElements());
        assertSame(item, resp.getContent().get(0));

        verify(loginLogRepository).findByStatusOrderByCreatedAtDesc(LoginStatus.FAILED, pageable);
        verify(loginLogMapper).toListItem(log);
    }

    @Test
    void listByIp_shouldReturnMappedPage() {
        Pageable pageable = PageRequest.of(0, 1);

        LoginLog log = LoginLog.builder().id(1L).build();
        Page<LoginLog> page = new PageImpl<>(List.of(log), pageable, 1);

        when(loginLogRepository.findByIpAddressOrderByCreatedAtDesc("1.1.1.1", pageable)).thenReturn(page);

        LoginLogListItemResponse item = LoginLogListItemResponse.builder().build();
        when(loginLogMapper.toListItem(log)).thenReturn(item);

        Page<LoginLogListItemResponse> resp = service.listByIp("1.1.1.1", pageable);

        assertEquals(1, resp.getTotalElements());
        assertSame(item, resp.getContent().get(0));

        verify(loginLogRepository).findByIpAddressOrderByCreatedAtDesc("1.1.1.1", pageable);
        verify(loginLogMapper).toListItem(log);
    }

    @Test
    void listByUserAndStatus_shouldThrowNotFound_whenUserMissing() {
        Pageable pageable = PageRequest.of(0, 10);
        when(userRepository.existsById(3L)).thenReturn(false);

        assertThrows(NotFoundException.class, () ->
                service.listByUserAndStatus(3L, LoginStatus.SUCCESS, pageable));

        verify(userRepository).existsById(3L);
        verifyNoInteractions(loginLogRepository, loginLogMapper);
    }

    @Test
    void listByUserAndStatus_shouldReturnMappedPage() {
        Pageable pageable = PageRequest.of(0, 1);
        when(userRepository.existsById(3L)).thenReturn(true);

        LoginLog log = LoginLog.builder().id(9L).status(LoginStatus.SUCCESS).build();
        Page<LoginLog> page = new PageImpl<>(List.of(log), pageable, 1);

        when(loginLogRepository.findByUser_IdAndStatusOrderByCreatedAtDesc(3L, LoginStatus.SUCCESS, pageable))
                .thenReturn(page);

        LoginLogListItemResponse item = LoginLogListItemResponse.builder().build();
        when(loginLogMapper.toListItem(log)).thenReturn(item);

        Page<LoginLogListItemResponse> resp =
                service.listByUserAndStatus(3L, LoginStatus.SUCCESS, pageable);

        assertEquals(1, resp.getTotalElements());
        assertSame(item, resp.getContent().get(0));

        verify(userRepository).existsById(3L);
        verify(loginLogRepository).findByUser_IdAndStatusOrderByCreatedAtDesc(3L, LoginStatus.SUCCESS, pageable);
        verify(loginLogMapper).toListItem(log);
    }

    @Test
    void listByDateRange_shouldReturnMappedPage() {
        Pageable pageable = PageRequest.of(0, 2);
        OffsetDateTime start = OffsetDateTime.parse("2025-01-01T00:00:00+00:00");
        OffsetDateTime end = OffsetDateTime.parse("2025-02-01T00:00:00+00:00");

        LoginLog l1 = LoginLog.builder().id(1L).build();
        LoginLog l2 = LoginLog.builder().id(2L).build();
        Page<LoginLog> page = new PageImpl<>(List.of(l1, l2), pageable, 2);

        when(loginLogRepository.findByCreatedAtBetweenOrderByCreatedAtDesc(start, end, pageable))
                .thenReturn(page);

        LoginLogListItemResponse r1 = LoginLogListItemResponse.builder().build();
        LoginLogListItemResponse r2 = LoginLogListItemResponse.builder().build();
        when(loginLogMapper.toListItem(l1)).thenReturn(r1);
        when(loginLogMapper.toListItem(l2)).thenReturn(r2);

        Page<LoginLogListItemResponse> resp =
                service.listByDateRange(start, end, pageable);

        assertEquals(2, resp.getTotalElements());
        assertSame(r1, resp.getContent().get(0));
        assertSame(r2, resp.getContent().get(1));

        verify(loginLogRepository).findByCreatedAtBetweenOrderByCreatedAtDesc(start, end, pageable);
        verify(loginLogMapper).toListItem(l1);
        verify(loginLogMapper).toListItem(l2);
    }

    @Test
    void getById_shouldReturnResponse() {
        LoginLog log = LoginLog.builder().id(5L).build();
        when(loginLogRepository.findById(5L)).thenReturn(Optional.of(log));

        LoginLogResponse expected = LoginLogResponse.builder().id(5L).build();
        when(loginLogMapper.toResponse(log)).thenReturn(expected);

        LoginLogResponse resp = service.getById(5L);

        assertNotNull(resp);
        assertEquals(5L, resp.getId());

        verify(loginLogRepository).findById(5L);
        verify(loginLogMapper).toResponse(log);
    }

    @Test
    void getById_shouldThrowNotFound_whenMissing() {
        when(loginLogRepository.findById(5L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.getById(5L));

        verify(loginLogRepository).findById(5L);
        verifyNoInteractions(loginLogMapper);
    }
}
