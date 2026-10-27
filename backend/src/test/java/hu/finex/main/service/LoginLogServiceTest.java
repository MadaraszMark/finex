package hu.finex.main.service;

import hu.finex.main.config.FinexProperties;
import hu.finex.main.dto.LoginLogListItemResponse;
import hu.finex.main.dto.LoginLogResponse;
import hu.finex.main.exception.BusinessException;
import hu.finex.main.exception.NotFoundException;
import hu.finex.main.mapper.LoginLogMapper;
import hu.finex.main.model.LoginLog;
import hu.finex.main.model.User;
import hu.finex.main.model.enums.LoginStatus;
import hu.finex.main.repository.LoginLogRepository;
import hu.finex.main.security.CurrentUser;
import hu.finex.main.util.DateUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoginLogServiceTest {

    @Mock private LoginLogRepository loginLogRepository;
    @Mock private LoginLogMapper loginLogMapper;
    @Mock private CurrentUser currentUser;
    @Spy private FinexProperties finexProperties = new FinexProperties();

    @InjectMocks private LoginLogService service;

    @Test
    void recordAttempt_shouldSaveMappedLog() {
        User user = User.builder().id(1L).build();
        LoginLog log = LoginLog.builder().user(user).email("a@finex.hu").status(LoginStatus.SUCCESS).build();
        when(loginLogMapper.toEntity(user, "a@finex.hu", "127.0.0.1", "UA", null, LoginStatus.SUCCESS)).thenReturn(log);

        service.recordAttempt(user, "a@finex.hu", LoginStatus.SUCCESS, "127.0.0.1", "UA", null);

        verify(loginLogRepository).save(log);
    }

    private static final String EMAIL = "a@finex.hu";
    private static final String IP = "10.0.0.1";
    private static final String LOCKED = LoginLogService.LOCKED_REASON;

    private void givenFailures(long fromThisIpOnAccount, long onAccount, long fromIp) {
        when(loginLogRepository.findFirstByEmailAndStatusOrderByCreatedAtDesc(EMAIL, LoginStatus.SUCCESS)).thenReturn(Optional.empty());
        lenient().when(loginLogRepository.countByEmailAndIpAddressAndStatusAndFailureReasonNotAndCreatedAtAfter(eq(EMAIL), eq(IP), eq(LoginStatus.FAILED), eq(LOCKED), any()))
                .thenReturn(fromThisIpOnAccount);
        lenient().when(loginLogRepository.countByEmailAndStatusAndFailureReasonNotAndCreatedAtAfter(eq(EMAIL), eq(LoginStatus.FAILED), eq(LOCKED), any()))
                .thenReturn(onAccount);
        lenient().when(loginLogRepository.countByIpAddressAndStatusAndFailureReasonNotAndCreatedAtAfter(eq(IP), eq(LoginStatus.FAILED), eq(LOCKED), any()))
                .thenReturn(fromIp);
    }

    @Test
    void isTemporarilyLocked_shouldBeTrue_whenTooManyFailuresFromThisIp() {
        givenFailures(5, 5, 5);

        assertTrue(service.isTemporarilyLocked(EMAIL, IP));
    }

    @Test
    void isTemporarilyLocked_shouldBeFalse_belowAllLimits() {
        givenFailures(4, 4, 4);

        assertFalse(service.isTemporarilyLocked(EMAIL, IP));
    }

    // Támadó a saját IP-címéről nem zárhatja ki a tulajdonost a fiókjából
    @Test
    void isTemporarilyLocked_shouldBeFalse_forOwnerOnOtherIp_whenAttackerFailedFromAnotherIp() {
        givenFailures(0, 5, 0);

        assertFalse(service.isTemporarilyLocked(EMAIL, IP));
    }

    @Test
    void isTemporarilyLocked_shouldBeTrue_whenAccountIsAttackedFromManyIps() {
        givenFailures(0, 20, 0);

        assertTrue(service.isTemporarilyLocked(EMAIL, IP));
    }

    @Test
    void isTemporarilyLocked_shouldBeTrue_whenIpTriesManyAccounts() {
        givenFailures(0, 0, 20);

        assertTrue(service.isTemporarilyLocked(EMAIL, IP));
    }

    @Test
    void isTemporarilyLocked_shouldCountOnlyFailuresAfterLastSuccess() {
        Instant lastSuccess = Instant.now().minus(2, ChronoUnit.MINUTES);
        when(loginLogRepository.findFirstByEmailAndStatusOrderByCreatedAtDesc(EMAIL, LoginStatus.SUCCESS))
                .thenReturn(Optional.of(LoginLog.builder().createdAt(lastSuccess).build()));
        when(loginLogRepository.countByEmailAndIpAddressAndStatusAndFailureReasonNotAndCreatedAtAfter(EMAIL, IP, LoginStatus.FAILED, LOCKED, lastSuccess)).thenReturn(1L);
        when(loginLogRepository.countByEmailAndStatusAndFailureReasonNotAndCreatedAtAfter(EMAIL, LoginStatus.FAILED, LOCKED, lastSuccess)).thenReturn(1L);
        when(loginLogRepository.countByIpAddressAndStatusAndFailureReasonNotAndCreatedAtAfter(eq(IP), eq(LoginStatus.FAILED), eq(LOCKED), any())).thenReturn(1L);

        assertFalse(service.isTemporarilyLocked(EMAIL, IP));
    }

    @Test
    void isTemporarilyLocked_shouldUseWindowStart_whenLastSuccessIsOlder() {
        Instant oldSuccess = Instant.now().minus(3, ChronoUnit.HOURS);
        when(loginLogRepository.findFirstByEmailAndStatusOrderByCreatedAtDesc(EMAIL, LoginStatus.SUCCESS))
                .thenReturn(Optional.of(LoginLog.builder().createdAt(oldSuccess).build()));

        ArgumentCaptor<Instant> sinceCaptor = ArgumentCaptor.forClass(Instant.class);
        when(loginLogRepository.countByEmailAndIpAddressAndStatusAndFailureReasonNotAndCreatedAtAfter(eq(EMAIL), eq(IP), eq(LoginStatus.FAILED), eq(LOCKED), sinceCaptor.capture())).thenReturn(0L);
        when(loginLogRepository.countByEmailAndStatusAndFailureReasonNotAndCreatedAtAfter(eq(EMAIL), eq(LoginStatus.FAILED), eq(LOCKED), any())).thenReturn(0L);
        when(loginLogRepository.countByIpAddressAndStatusAndFailureReasonNotAndCreatedAtAfter(eq(IP), eq(LoginStatus.FAILED), eq(LOCKED), any())).thenReturn(0L);

        service.isTemporarilyLocked(EMAIL, IP);

        // A 15 perces zárolási ablak eleje számít, nem a régi sikeres belépés
        assertTrue(sinceCaptor.getValue().isAfter(Instant.now().minus(16, ChronoUnit.MINUTES)));
    }

    @Test
    void listMine_shouldReturnMappedPage() {
        when(currentUser.requireId()).thenReturn(7L);

        Pageable pageable = PageRequest.of(0, 10);
        LoginLog log = LoginLog.builder().id(1L).build();
        when(loginLogRepository.findByUser_IdOrderByCreatedAtDesc(7L, pageable)).thenReturn(new PageImpl<>(List.of(log), pageable, 1));
        when(loginLogMapper.toListItem(log)).thenReturn(LoginLogListItemResponse.builder().status(LoginStatus.SUCCESS).build());

        Page<LoginLogListItemResponse> page = service.listMine(pageable);

        assertEquals(1, page.getTotalElements());
        assertEquals(LoginStatus.SUCCESS, page.getContent().get(0).getStatus());
    }

    @Test
    void search_shouldConvertDatesAndTrimIp() {
        Pageable pageable = PageRequest.of(0, 10);
        LocalDate from = LocalDate.of(2025, 3, 1);
        LocalDate to = LocalDate.of(2025, 3, 31);

        LoginLog log = LoginLog.builder().id(1L).build();
        when(loginLogRepository.search(LoginStatus.FAILED, 7L, "10.0.0.1", DateUtils.startOfDay(from), DateUtils.startOfDay(LocalDate.of(2025, 4, 1)), pageable))
                .thenReturn(new PageImpl<>(List.of(log), pageable, 1));
        when(loginLogMapper.toResponse(log)).thenReturn(LoginLogResponse.builder().id(1L).build());

        Page<LoginLogResponse> page = service.search(LoginStatus.FAILED, 7L, " 10.0.0.1 ", from, to, pageable);

        assertEquals(1, page.getTotalElements());
    }

    @Test
    void search_shouldUseOpenPeriod_whenNoFilter() {
        Pageable pageable = PageRequest.of(0, 10);
        when(loginLogRepository.search(null, null, null, DateUtils.BEGINNING_OF_TIME, DateUtils.END_OF_TIME, pageable)).thenReturn(Page.empty(pageable));

        Page<LoginLogResponse> page = service.search(null, null, "  ", null, null, pageable);

        assertEquals(0, page.getTotalElements());
    }

    @Test
    void search_shouldThrowBusinessException_whenFromIsAfterTo() {
        assertThrows(BusinessException.class, () -> service.search(null, null, null, LocalDate.of(2025, 4, 1), LocalDate.of(2025, 3, 1), PageRequest.of(0, 10)));

        verifyNoInteractions(loginLogRepository);
    }

    @Test
    void getById_shouldReturnResponse() {
        LoginLog log = LoginLog.builder().id(10L).build();
        when(loginLogRepository.findById(10L)).thenReturn(Optional.of(log));

        LoginLogResponse expected = LoginLogResponse.builder().id(10L).build();
        when(loginLogMapper.toResponse(log)).thenReturn(expected);

        LoginLogResponse resp = service.getById(10L);

        assertEquals(10L, resp.getId());
        verify(loginLogRepository).findById(10L);
        verify(loginLogMapper).toResponse(log);
    }

    @Test
    void getById_shouldThrowNotFound_whenMissing() {
        when(loginLogRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.getById(10L));

        verify(loginLogRepository).findById(10L);
        verifyNoInteractions(loginLogMapper);
    }
}
