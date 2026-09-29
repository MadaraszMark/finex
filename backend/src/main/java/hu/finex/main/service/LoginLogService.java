package hu.finex.main.service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

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
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LoginLogService {

    private final LoginLogRepository loginLogRepository;
    private final LoginLogMapper loginLogMapper;
    private final CurrentUser currentUser;
    private final FinexProperties finexProperties;

    // Saját tranzakcióban ment (REQUIRES_NEW): a sikertelen próbálkozás akkor is naplózva marad,
    // ha a bejelentkezés utána kivétellel megszakad és a hívó tranzakciója visszagörgetődik
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordAttempt(User user, String email, LoginStatus status, String ip, String userAgent, String failureReason) {
        LoginLog log = loginLogMapper.toEntity(user, email, ip, userAgent, failureReason, status);
        loginLogRepository.save(log);
    }

    // Átmeneti zárolás: az utolsó sikeres belépés óta (de legfeljebb a zárolási időablakon belül) túl sok sikertelen próbálkozás
    @Transactional(readOnly = true)
    public boolean isTemporarilyLocked(String email) {
        Instant windowStart = Instant.now().minus(finexProperties.getLoginLockMinutes(), ChronoUnit.MINUTES);

        Instant since = loginLogRepository.findFirstByEmailAndStatusOrderByCreatedAtDesc(email, LoginStatus.SUCCESS)
                .map(LoginLog::getCreatedAt)
                .filter(lastSuccess -> lastSuccess.isAfter(windowStart))
                .orElse(windowStart);

        long failedAttempts = loginLogRepository.countByEmailAndStatusAndCreatedAtAfter(email, LoginStatus.FAILED, since);
        return failedAttempts >= finexProperties.getLoginMaxFailedAttempts();
    }

    // A bejelentkezett felhasználó saját belépési előzményei (eszköz, IP, sikeres/sikertelen)
    @Transactional(readOnly = true)
    public Page<LoginLogListItemResponse> listMine(Pageable pageable) {
        return loginLogRepository.findByUser_IdOrderByCreatedAtDesc(currentUser.requireId(), pageable).map(loginLogMapper::toListItem);
    }

    // Admin: keresés státusz, felhasználó, IP és időszak szerint (minden szűrő opcionális)
    @Transactional(readOnly = true)
    public Page<LoginLogResponse> search(LoginStatus status, Long userId, String ipAddress, LocalDate from, LocalDate to, Pageable pageable) {
        if (from != null && to != null && from.isAfter(to)) {
            throw new BusinessException("A kezdő dátum nem lehet későbbi a záró dátumnál.");
        }

        Instant fromInstant = from != null ? DateUtils.startOfDay(from) : DateUtils.BEGINNING_OF_TIME;
        Instant toInstant = to != null ? DateUtils.startOfDay(to.plusDays(1)) : DateUtils.END_OF_TIME;
        String ip = StringUtils.hasText(ipAddress) ? ipAddress.trim() : null;

        return loginLogRepository.search(status, userId, ip, fromInstant, toInstant, pageable).map(loginLogMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public LoginLogResponse getById(Long id) {
        LoginLog log = loginLogRepository.findById(id).orElseThrow(() -> new NotFoundException("Login log nem található."));

        return loginLogMapper.toResponse(log);
    }
}
