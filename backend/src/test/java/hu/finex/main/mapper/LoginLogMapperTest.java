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
                "anna@finex.hu",
                "192.168.0.1",
                "Mozilla/5.0",
                "Hibás jelszó",
                LoginStatus.FAILED
        );

        assertNotNull(log);
        assertNull(log.getId());
        assertEquals(user, log.getUser());
        assertEquals("anna@finex.hu", log.getEmail());
        assertEquals("192.168.0.1", log.getIpAddress());
        assertEquals("Mozilla/5.0", log.getUserAgent());
        assertEquals("Hibás jelszó", log.getFailureReason());
        assertEquals(LoginStatus.FAILED, log.getStatus());
    }

    @Test
    void testToEntity_withUnknownUser() {
        LoginLog log = mapper.toEntity(null, "nincs@finex.hu", "10.0.0.1", "curl", "Ismeretlen e-mail cím", LoginStatus.FAILED);

        assertNull(log.getUser());
        assertEquals("nincs@finex.hu", log.getEmail());
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
                .email("bence@finex.hu")
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
        assertEquals("bence@finex.hu", response.getEmail());
        assertEquals(LoginStatus.SUCCESS, response.getStatus());
        assertEquals("10.0.0.5", response.getIpAddress());
        assertEquals("Chrome", response.getUserAgent());
        assertNull(response.getFailureReason());
        assertEquals(createdAt, response.getCreatedAt());
    }

    @Test
    void testToResponse_withoutUser() {
        LoginLog log = LoginLog.builder()
                .id(56L)
                .email("nincs@finex.hu")
                .status(LoginStatus.FAILED)
                .build();

        LoginLogResponse response = mapper.toResponse(log);

        assertNull(response.getUserId());
        assertEquals("nincs@finex.hu", response.getEmail());
    }

    @Test
    void testToListItem() {
        Instant createdAt = Instant.parse("2025-01-06T18:00:00Z");

        LoginLog log = LoginLog.builder()
                .status(LoginStatus.FAILED)
                .ipAddress("172.16.0.10")
                .userAgent("Firefox")
                .failureReason("Hibás jelszó")
                .createdAt(createdAt)
                .build();

        LoginLogListItemResponse response = mapper.toListItem(log);

        assertNotNull(response);
        assertEquals(LoginStatus.FAILED, response.getStatus());
        assertEquals("172.16.0.10", response.getIpAddress());
        assertEquals("Firefox", response.getUserAgent());
        assertEquals("Hibás jelszó", response.getFailureReason());
        assertEquals(createdAt, response.getCreatedAt());
    }
}
