package hu.finex.main.mapper;

import hu.finex.main.dto.LoginLogListItemResponse;
import hu.finex.main.dto.LoginLogResponse;
import hu.finex.main.model.LoginLog;
import hu.finex.main.model.User;
import hu.finex.main.model.enums.LoginStatus;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class LoginLogMapperTest {

    private final LoginLogMapper mapper = new LoginLogMapper();

    @Test
    void testToEntity() {
        User user = User.builder()
                .id(1L)
                .build();

        LoginLog log = mapper.toEntity(
                user,
                "192.168.0.1",
                "Mozilla/5.0",
                "Invalid password",
                LoginStatus.FAILED
        );

        assertNotNull(log);
        assertNull(log.getId());
        assertEquals(user, log.getUser());
        assertEquals("192.168.0.1", log.getIpAddress());
        assertEquals("Mozilla/5.0", log.getUserAgent());
        assertEquals("Invalid password", log.getFailureReason());
        assertEquals(LoginStatus.FAILED, log.getStatus());
    }

    @Test
    void testToResponse() {
        Instant createdAt = Instant.parse("2025-01-05T09:15:00Z");

        User user = User.builder()
                .id(8L)
                .build();

        LoginLog log = LoginLog.builder()
                .id(55L)
                .user(user)
                .status(LoginStatus.SUCCESS)
                .ipAddress("10.0.0.5")
                .userAgent("Chrome")
                .failureReason(null)
                .createdAt(createdAt)
                .build();

        LoginLogResponse response = mapper.toResponse(log);

        assertNotNull(response);
        assertEquals(55L, response.getId());
        assertEquals(8L, response.getUserId());
        assertEquals(LoginStatus.SUCCESS, response.getStatus());
        assertEquals("10.0.0.5", response.getIpAddress());
        assertEquals("Chrome", response.getUserAgent());
        assertNull(response.getFailureReason());
        assertEquals(createdAt, response.getCreatedAt());
    }

    @Test
    void testToListItem() {
        Instant createdAt = Instant.parse("2025-01-06T18:00:00Z");

        LoginLog log = LoginLog.builder()
                .status(LoginStatus.FAILED)
                .ipAddress("172.16.0.10")
                .createdAt(createdAt)
                .build();

        LoginLogListItemResponse response = mapper.toListItem(log);

        assertNotNull(response);
        assertEquals(LoginStatus.FAILED, response.getStatus());
        assertEquals("172.16.0.10", response.getIpAddress());
        assertEquals(createdAt, response.getCreatedAt());
    }
}
