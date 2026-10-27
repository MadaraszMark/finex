package hu.finex.main.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HexFormat;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import hu.finex.main.exception.BusinessException;
import hu.finex.main.model.IdempotencyKey;
import hu.finex.main.repository.IdempotencyKeyRepository;
import hu.finex.main.security.CurrentUser;
import lombok.RequiredArgsConstructor;

// Idempotencia: azonos Idempotency-Key-jel újraküldött pénzmozgató kérés csak egyszer könyvelődik

@Service
@RequiredArgsConstructor
public class IdempotencyService {

    private static final Pattern KEY_FORMAT = Pattern.compile("^[A-Za-z0-9_-]{8,100}$");

    // Ennyi nap után a kulcsot az ütemező törli (egy újraküldés ennél sokkal hamarabb megtörténik)
    private static final int RETENTION_DAYS = 7;

    private final IdempotencyKeyRepository idempotencyKeyRepository;
    private final CurrentUser currentUser;
    private final ObjectMapper objectMapper;

    // Kulcs nélkül egyszerűen végrehajtja a műveletet (a fejléc nem kötelező)
    @Transactional
    public <T> T execute(String key, String operation, Object request, Class<T> responseType, Supplier<T> action) {
        if (key == null) {
            return action.get();
        }
        if (!KEY_FORMAT.matcher(key).matches()) {
            throw new BusinessException("Érvénytelen Idempotency-Key (8–100 karakter: betű, szám, kötőjel, aláhúzás).");
        }

        Long userId = currentUser.requireId();
        String requestHash = sha256(operation + ":" + toJson(request));

        if (idempotencyKeyRepository.insertIfAbsent(userId, key, requestHash) == 0) {
            IdempotencyKey existing = idempotencyKeyRepository.findByUserIdAndIdempotencyKey(userId, key)
                    .orElseThrow(() -> new BusinessException("Ez a kérés még feldolgozás alatt van, próbáld újra."));

            if (!existing.getRequestHash().equals(requestHash)) {
                throw new BusinessException("Ezt az Idempotency-Key-t már egy másik kéréshez használtad.");
            }
            if (existing.getResponseBody() == null) {
                throw new BusinessException("Ez a kérés még feldolgozás alatt van, próbáld újra.");
            }
            return fromJson(existing.getResponseBody(), responseType);
        }

        T result = action.get();

        IdempotencyKey saved = idempotencyKeyRepository.findByUserIdAndIdempotencyKey(userId, key)
                .orElseThrow(() -> new IllegalStateException("A lefoglalt idempotencia-kulcs eltűnt."));
        saved.setResponseBody(toJson(result));

        return result;
    }

    // A régi kulcsok törlése (az ütemező hívja)
    @Transactional
    public int deleteExpired() {
        return idempotencyKeyRepository.deleteCreatedBefore(Instant.now().minus(RETENTION_DAYS, ChronoUnit.DAYS));
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Nem sikerült JSON-ná alakítani.", e);
        }
    }

    private <T> T fromJson(String json, Class<T> type) {
        try {
            return objectMapper.readValue(json, type);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Nem sikerült beolvasni a tárolt választ.", e);
        }
    }

    private static String sha256(String value) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
