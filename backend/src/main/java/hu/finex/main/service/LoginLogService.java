package hu.finex.main.service;

import hu.finex.main.dto.LoginLogListItemResponse;
import hu.finex.main.dto.LoginLogResponse;
import hu.finex.main.exception.NotFoundException;
import hu.finex.main.mapper.LoginLogMapper;
import hu.finex.main.model.LoginLog;
import hu.finex.main.model.enums.LoginStatus;
import hu.finex.main.repository.LoginLogRepository;
import hu.finex.main.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class LoginLogService {

    private final LoginLogRepository loginLogRepository;
    private final UserRepository userRepository;
    private final LoginLogMapper loginLogMapper;

    @Transactional(readOnly = true)
    public Page<LoginLogListItemResponse> listByUser(Long userId, Pageable pageable) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Felhasználó nem található.");
        }

        return loginLogRepository.findByUser_IdOrderByCreatedAtDesc(userId, pageable).map(loginLogMapper::toListItem);
    }

    @Transactional(readOnly = true)
    public Page<LoginLogListItemResponse> listByStatus(LoginStatus status, Pageable pageable) {
        return loginLogRepository.findByStatusOrderByCreatedAtDesc(status, pageable).map(loginLogMapper::toListItem);
    }

    @Transactional(readOnly = true)
    public Page<LoginLogListItemResponse> listByIp(String ip, Pageable pageable) {
        return loginLogRepository.findByIpAddressOrderByCreatedAtDesc(ip, pageable).map(loginLogMapper::toListItem);
    }

    @Transactional(readOnly = true)
    public Page<LoginLogListItemResponse> listByUserAndStatus(Long userId, LoginStatus status, Pageable pageable) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Felhasználó nem található.");
        }

        return loginLogRepository.findByUser_IdAndStatusOrderByCreatedAtDesc(userId, status, pageable).map(loginLogMapper::toListItem);
    }

    @Transactional(readOnly = true)
    public Page<LoginLogListItemResponse> listByDateRange(OffsetDateTime start, OffsetDateTime end, Pageable pageable) {

        return loginLogRepository.findByCreatedAtBetweenOrderByCreatedAtDesc(start, end, pageable).map(loginLogMapper::toListItem);
    }

    @Transactional(readOnly = true)
    public LoginLogResponse getById(Long id) {
        LoginLog log = loginLogRepository.findById(id).orElseThrow(() -> new NotFoundException("Login log nem található."));

        return loginLogMapper.toResponse(log);
    }
}
