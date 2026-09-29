package hu.finex.main.security;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import hu.finex.main.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtTokenUtil {

    private final SecretKey key;
    private final long expirationMs;

    public JwtTokenUtil(
            @Value("${auth.jwt.secret}") String secret,
            @Value("${auth.jwt.expiration-ms:3600000}") long expirationMs) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    // A token tárgya (subject) a felhasználó azonosítója, így e-mail-csere után is érvényes marad.
    // Az e-mail és a szerepkör csak tájékoztató adat (a frontendnek), a jogosultságot a szerver mindig az adatbázisból veszi.
    public String generateToken(User user) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .setSubject(String.valueOf(user.getId()))
                .claim("email", user.getEmail())
                .claim("role", user.getRole().name())
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + expirationMs))
                .signWith(key)
                .compact();
    }

    // null, ha a subject nem felhasználó-azonosító (pl. egy régi, e-mail alapú token)
    public Long extractUserId(String token) {
        try {
            return Long.valueOf(parse(token).getBody().getSubject());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public Instant extractExpiration(String token) {
        return parse(token).getBody().getExpiration().toInstant();
    }

    public boolean isValid(String token) {
        try {
            parse(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private Jws<Claims> parse(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token);
    }
}
