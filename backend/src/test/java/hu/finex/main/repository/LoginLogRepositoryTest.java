package hu.finex.main.repository;

import hu.finex.main.model.LoginLog;
import hu.finex.main.model.User;
import hu.finex.main.model.enums.LoginStatus;
import hu.finex.main.model.enums.UserRole;
import hu.finex.main.model.enums.UserStatus;
import hu.finex.main.util.DateUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class LoginLogRepositoryTest extends PostgresRepositoryTestBase {

    @Autowired private LoginLogRepository loginLogRepository;
    @Autowired private UserRepository userRepository;

    @Test
    void search_shouldCombineOptionalFilters() {
        User user = saveUser("log1@test.hu");
        saveLog(user, "log1@test.hu", LoginStatus.SUCCESS, "10.0.0.1", "2025-03-01T08:00:00Z");
        saveLog(user, "log1@test.hu", LoginStatus.FAILED, "10.0.0.2", "2025-03-02T08:00:00Z");
        saveLog(null, "nincs@test.hu", LoginStatus.FAILED, "10.0.0.2", "2025-03-03T08:00:00Z");

        PageRequest page = PageRequest.of(0, 20);

        Page<LoginLog> all = loginLogRepository.search(null, null, null, DateUtils.BEGINNING_OF_TIME, DateUtils.END_OF_TIME, page);
        Page<LoginLog> failed = loginLogRepository.search(LoginStatus.FAILED, null, null, DateUtils.BEGINNING_OF_TIME, DateUtils.END_OF_TIME, page);
        Page<LoginLog> byUser = loginLogRepository.search(null, user.getId(), null, DateUtils.BEGINNING_OF_TIME, DateUtils.END_OF_TIME, page);
        Page<LoginLog> byIp = loginLogRepository.search(null, null, "10.0.0.2", DateUtils.BEGINNING_OF_TIME, DateUtils.END_OF_TIME, page);
        Page<LoginLog> byPeriod = loginLogRepository.search(null, null, null, Instant.parse("2025-03-02T00:00:00Z"), Instant.parse("2025-03-03T00:00:00Z"), page);

        assertEquals(3, all.getTotalElements());
        assertEquals("nincs@test.hu", all.getContent().get(0).getEmail());
        assertEquals(2, failed.getTotalElements());
        assertEquals(2, byUser.getTotalElements());
        assertEquals(2, byIp.getTotalElements());
        assertEquals(1, byPeriod.getTotalElements());
    }

    @Test
    void countAndLastSuccess_shouldSupportTemporaryLock() {
        User user = saveUser("log2@test.hu");
        saveLog(user, "log2@test.hu", LoginStatus.SUCCESS, "10.0.0.1", "2025-03-01T08:00:00Z");
        saveLog(user, "log2@test.hu", LoginStatus.FAILED, "10.0.0.1", "2025-03-01T09:00:00Z");
        saveLog(user, "log2@test.hu", LoginStatus.FAILED, "10.0.0.1", "2025-03-01T09:01:00Z");

        Optional<LoginLog> lastSuccess = loginLogRepository.findFirstByEmailAndStatusOrderByCreatedAtDesc("log2@test.hu", LoginStatus.SUCCESS);

        assertTrue(lastSuccess.isPresent());
        assertEquals(Instant.parse("2025-03-01T08:00:00Z"), lastSuccess.get().getCreatedAt());
        assertEquals(2, loginLogRepository.countByEmailAndStatusAndCreatedAtAfter("log2@test.hu", LoginStatus.FAILED, lastSuccess.get().getCreatedAt()));
        assertEquals(0, loginLogRepository.countByEmailAndStatusAndCreatedAtAfter("log2@test.hu", LoginStatus.FAILED, Instant.parse("2025-03-01T10:00:00Z")));
    }

    private User saveUser(String email) {
        User user = User.builder()
                .firstName("Test")
                .lastName("User")
                .email(email)
                .passwordHash("HASH")
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        return userRepository.saveAndFlush(user);
    }

    private LoginLog saveLog(User user, String email, LoginStatus status, String ip, String createdAt) {
        LoginLog log = LoginLog.builder()
                .user(user)
                .email(email)
                .status(status)
                .ipAddress(ip)
                .userAgent("JUnit")
                .createdAt(Instant.parse(createdAt))
                .build();
        return loginLogRepository.saveAndFlush(log);
    }
}
