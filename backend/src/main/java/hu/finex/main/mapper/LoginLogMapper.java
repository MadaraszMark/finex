package hu.finex.main.mapper;

import org.springframework.stereotype.Component;

import hu.finex.main.dto.LoginLogListItemResponse;
import hu.finex.main.dto.LoginLogResponse;
import hu.finex.main.model.LoginLog;
import hu.finex.main.model.User;
import hu.finex.main.model.enums.LoginStatus;

@Component
public class LoginLogMapper {

    // A user null lehet: ismeretlen e-mail címmel érkező próbálkozás is naplózásra kerül
    public LoginLog toEntity(User user, String email, String ip, String userAgent, String failureReason, LoginStatus status) {
        return LoginLog.builder()
                .user(user)
                .email(email)
                .ipAddress(ip)
                .userAgent(userAgent)
                .failureReason(failureReason)
                .status(status)
                .build();
    }

    public LoginLogResponse toResponse(LoginLog log) {
        return LoginLogResponse.builder()
                .id(log.getId())
                .userId(log.getUser() != null ? log.getUser().getId() : null)
                .email(log.getEmail())
                .status(log.getStatus())
                .ipAddress(log.getIpAddress())
                .userAgent(log.getUserAgent())
                .failureReason(log.getFailureReason())
                .createdAt(log.getCreatedAt())
                .build();
    }

    public LoginLogListItemResponse toListItem(LoginLog log) {
        return LoginLogListItemResponse.builder()
                .status(log.getStatus())
                .ipAddress(log.getIpAddress())
                .userAgent(log.getUserAgent())
                .failureReason(log.getFailureReason())
                .createdAt(log.getCreatedAt())
                .build();
    }
}
