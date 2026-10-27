package hu.finex.main.security;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;

import hu.finex.main.config.FinexProperties;
import hu.finex.main.exception.ApiError;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

// Kérésszám-korlát IP-címenként a belépésre és a regisztrációra (csúszó időablak, memóriában)

public class RateLimitFilter extends OncePerRequestFilter {

    // Egy JSON-kérés legfeljebb ekkora lehet (a leghosszabb mező is pár ezer karakter)
    public static final long MAX_REQUEST_BYTES = 64 * 1024;

    private static final long MINUTE_MS = 60_000;
    private static final long HOUR_MS = 60 * MINUTE_MS;

    // Ennyi kulcs fölött a lejárt bejegyzések törlődnek, hogy a memória ne nőjön korlátlanul
    private static final int CLEANUP_THRESHOLD = 10_000;

    private final FinexProperties finexProperties;
    private final ObjectMapper objectMapper;
    private final Map<String, Deque<Long>> hits = new ConcurrentHashMap<>();

    public RateLimitFilter(FinexProperties finexProperties, ObjectMapper objectMapper) {
        this.finexProperties = finexProperties;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if (request.getContentLengthLong() > MAX_REQUEST_BYTES) {
            reject(response, HttpStatus.PAYLOAD_TOO_LARGE, "A kérés túl nagy.");
            return;
        }

        if ("POST".equals(request.getMethod())) {
            String path = request.getRequestURI().substring(request.getContextPath().length());
            String ip = request.getRemoteAddr();

            if ("/auth/login".equals(path) && !allow("login:" + ip, finexProperties.getRateLimitLoginPerMinute(), MINUTE_MS)) {
                reject(response, HttpStatus.TOO_MANY_REQUESTS, "Túl sok bejelentkezési kérés. Várj egy percet, és próbáld újra.");
                return;
            }
            if ("/auth/register".equals(path) && !allow("register:" + ip, finexProperties.getRateLimitRegisterPerHour(), HOUR_MS)) {
                reject(response, HttpStatus.TOO_MANY_REQUESTS, "Túl sok regisztráció erről a címről. Próbáld újra később.");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    // Igaz, ha a kulcs (végpont + IP) még nem érte el a korlátot; ekkor ez a kérés is beleszámít
    boolean allow(String key, int limit, long windowMs) {
        long now = System.currentTimeMillis();
        if (hits.size() > CLEANUP_THRESHOLD) {
            hits.entrySet().removeIf(entry -> isStale(entry.getValue(), now, HOUR_MS));
        }

        Deque<Long> timestamps = hits.computeIfAbsent(key, k -> new ArrayDeque<>());
        synchronized (timestamps) {
            while (!timestamps.isEmpty() && now - timestamps.peekFirst() >= windowMs) {
                timestamps.pollFirst();
            }
            if (timestamps.size() >= limit) {
                return false;
            }
            timestamps.addLast(now);
            return true;
        }
    }

    private static boolean isStale(Deque<Long> timestamps, long now, long windowMs) {
        synchronized (timestamps) {
            return timestamps.isEmpty() || now - timestamps.peekLast() >= windowMs;
        }
    }

    private void reject(HttpServletResponse response, HttpStatus status, String message) throws IOException {
        ApiError error = ApiError.builder()
                .timestamp(OffsetDateTime.now())
                .status(status.value())
                .message(message)
                .build();

        response.setStatus(status.value());
        response.setCharacterEncoding("UTF-8");
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        if (status == HttpStatus.TOO_MANY_REQUESTS) {
            response.setHeader("Retry-After", "60");
        }
        objectMapper.writeValue(response.getOutputStream(), error);
    }
}
