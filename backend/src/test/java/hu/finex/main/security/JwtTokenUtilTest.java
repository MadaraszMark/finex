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
    void generateToken_shouldCarryTokenVersion_andUniqueId() {
        User user = User.builder().id(42L).email("anna@finex.hu").role(UserRole.USER).tokenVersion(7).build();

        String first = jwtTokenUtil.generateToken(user);
        String second = jwtTokenUtil.generateToken(user);

        assertEquals(7, jwtTokenUtil.extractTokenVersion(first));
        assertNotNull(jwtTokenUtil.extractTokenId(first));
        assertNotEquals(jwtTokenUtil.extractTokenId(first), jwtTokenUtil.extractTokenId(second));
    }

    // A publikus repóban nincs titkos kulcs, ezért megadás nélkül véletlen kulcs készül
    @Test
    void missingSecret_shouldUseRandomKey_soForeignTokensAreRejected() {
        JwtTokenUtil randomA = new JwtTokenUtil("", 3600000);
        JwtTokenUtil randomB = new JwtTokenUtil(null, 3600000);
        User user = User.builder().id(42L).email("anna@finex.hu").role(UserRole.ADMIN).build();

        String token = randomA.generateToken(user);

        assertTrue(randomA.isValid(token));
        assertFalse(randomB.isValid(token));
        assertFalse(jwtTokenUtil.isValid(token));
    }

    @Test
    void shortSecret_shouldBeRejectedAtStartup() {
        assertThrows(IllegalStateException.class, () -> new JwtTokenUtil("rovid-titok", 3600000));
    }

    @Test
    void isValid_shouldRejectUnsignedToken() {
        String header = java.util.Base64.getUrlEncoder().withoutPadding().encodeToString("{\"alg\":\"none\"}".getBytes());
        String payload = java.util.Base64.getUrlEncoder().withoutPadding().encodeToString("{\"sub\":\"1\",\"ver\":0,\"jti\":\"x\"}".getBytes());

        assertFalse(jwtTokenUtil.isValid(header + "." + payload + "."));
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
