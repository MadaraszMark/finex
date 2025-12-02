package hu.finex.main.mapper;

import org.springframework.stereotype.Component;

import hu.finex.main.dto.LoginLogListItemResponse;
import hu.finex.main.dto.LoginLogResponse;
import hu.finex.main.model.LoginLog;
import hu.finex.main.model.User;
import hu.finex.main.model.enums.LoginStatus;

@Component
public class LoginLogMapper {

    public LoginLog toEntity(User user, String ip, String userAgent, String failureReason, LoginStatus status) {
        return LoginLog.builder()
                .user(user)
                .ipAddress(ip)
                .userAgent(userAgent)
                .failureReason(failureReason)
                .status(status)
                .build();
    }

    public LoginLogResponse toResponse(LoginLog log) {
        return LoginLogResponse.builder()
                .id(log.getId())
                .userId(log.getUser().getId())
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
                .createdAt(log.getCreatedAt())
                .build();
    }
}
