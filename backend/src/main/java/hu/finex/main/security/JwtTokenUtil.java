package hu.finex.main.security;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import hu.finex.main.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class JwtTokenUtil {

    // HS256-hoz legalább 256 bites (32 bájtos) kulcs kell, különben a token aláírása kitalálható
    private static final int MIN_SECRET_BYTES = 32;

    private final SecretKey key;
    private final long expirationMs;

    // A kulcs a FINEX_JWT_SECRET változóból jön; ha nincs megadva, minden induláskor véletlen kulcs készül
    public JwtTokenUtil(
            @Value("${auth.jwt.secret:}") String secret,
            @Value("${auth.jwt.expiration-ms:3600000}") long expirationMs) {
        this.key = Keys.hmacShaKeyFor(resolveSecret(secret));
        this.expirationMs = expirationMs;
    }

    // A "ver" a tokenverzió: jelszócsere, letiltás vagy kijelentkezés után a régi token érvénytelen
    public String generateToken(User user) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(String.valueOf(user.getId()))
                .claim("email", user.getEmail())
                .claim("role", user.getRole().name())
                .claim("ver", user.getTokenVersion())
                .issuedAt(new Date(now))
                .expiration(new Date(now + expirationMs))
                .signWith(key)
                .compact();
    }

    // null, ha a subject nem felhasználó-azonosító
    public Long extractUserId(String token) {
        try {
            return Long.valueOf(parse(token).getSubject());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // null, ha a tokenben nincs verzió: az ilyen token nem fogadható el
    public Integer extractTokenVersion(String token) {
        Object version = parse(token).get("ver");
        return version instanceof Number number ? number.intValue() : null;
    }

    public String extractTokenId(String token) {
        return parse(token).getId();
    }

    public Instant extractExpiration(String token) {
        return parse(token).getExpiration().toInstant();
    }

    public boolean isValid(String token) {
        try {
            parse(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    // Csak a saját kulcsunkkal aláírt HMAC tokent fogadja el ("alg": "none" esetén is kivételt dob)
    private Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private static byte[] resolveSecret(String secret) {
        if (!StringUtils.hasText(secret)) {
            log.warn("A FINEX_JWT_SECRET nincs beállítva: véletlen JWT-kulcs készült, újraindítás után újra be kell jelentkezni.");
            byte[] random = new byte[64];
            new SecureRandom().nextBytes(random);
            return random;
        }

        byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < MIN_SECRET_BYTES) {
            throw new IllegalStateException("A FINEX_JWT_SECRET legalább " + MIN_SECRET_BYTES + " bájt hosszú legyen.");
        }
        return bytes;
    }
}
