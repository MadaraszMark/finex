package hu.finex.main.security;

import hu.finex.main.model.User;
import hu.finex.main.model.enums.UserRole;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenUtilTest {

    private static final String SECRET = "TestSecretKeyForFinexJwtTokens1234567890";

    private final JwtTokenUtil jwtTokenUtil = new JwtTokenUtil(SECRET, 3600000);

    @Test
    void generateToken_shouldUseUserIdAsSubject() {
        User user = User.builder().id(42L).email("anna@finex.hu").role(UserRole.USER).build();

        String token = jwtTokenUtil.generateToken(user);

        assertTrue(jwtTokenUtil.isValid(token));
        assertEquals(42L, jwtTokenUtil.extractUserId(token));
    }

    @Test
    void extractExpiration_shouldBeOneHourLater() {
        User user = User.builder().id(42L).email("anna@finex.hu").role(UserRole.ADMIN).build();

        String token = jwtTokenUtil.generateToken(user);
        Instant expiresAt = jwtTokenUtil.extractExpiration(token);

        Duration untilExpiry = Duration.between(Instant.now(), expiresAt);
        assertTrue(untilExpiry.toMinutes() >= 59 && untilExpiry.toMinutes() <= 60);
    }

    @Test
    void isValid_shouldRejectTokenSignedWithOtherKey() {
        JwtTokenUtil otherUtil = new JwtTokenUtil("AnotherSecretKeyForFinexJwtTokens987654321", 3600000);
        User user = User.builder().id(42L).email("anna@finex.hu").role(UserRole.USER).build();

        String foreignToken = otherUtil.generateToken(user);

        assertFalse(jwtTokenUtil.isValid(foreignToken));
    }

    @Test
    void isValid_shouldRejectExpiredOrGarbageToken() {
        JwtTokenUtil expiredUtil = new JwtTokenUtil(SECRET, -1000);
        User user = User.builder().id(42L).email("anna@finex.hu").role(UserRole.USER).build();

        assertFalse(jwtTokenUtil.isValid(expiredUtil.generateToken(user)));
        assertFalse(jwtTokenUtil.isValid("nem.egy.token"));
        assertFalse(jwtTokenUtil.isValid(""));
    }
}
