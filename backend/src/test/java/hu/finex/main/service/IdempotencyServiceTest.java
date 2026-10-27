package hu.finex.main.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import hu.finex.main.dto.TransferRequest;
import hu.finex.main.dto.TransferResponse;
import hu.finex.main.exception.BusinessException;
import hu.finex.main.model.IdempotencyKey;
import hu.finex.main.repository.IdempotencyKeyRepository;
import hu.finex.main.security.CurrentUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IdempotencyServiceTest {

    private static final String KEY = "6f1c2a90-aa11-4c55-9a3e-0d7f5d1f2b10";

    @Mock private IdempotencyKeyRepository idempotencyKeyRepository;
    @Mock private CurrentUser currentUser;
    @Spy private ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @InjectMocks private IdempotencyService service;

    private final TransferRequest request = TransferRequest.builder().fromAccountId(1L).toAccountNumber("HU00").partnerName("X").amount(BigDecimal.TEN).build();
    private final AtomicInteger bookings = new AtomicInteger();
    private final Supplier<TransferResponse> action = () -> {
        bookings.incrementAndGet();
        return TransferResponse.builder().transactionId(100L).amount(BigDecimal.TEN).build();
    };

    @BeforeEach
    void resetCounter() {
        bookings.set(0);
    }

    @Test
    void execute_shouldJustRun_whenNoKeyIsGiven() {
        TransferResponse resp = service.execute(null, "transfer", request, TransferResponse.class, action);

        assertEquals(100L, resp.getTransactionId());
        assertEquals(1, bookings.get());
        verifyNoInteractions(idempotencyKeyRepository);
    }

    @Test
    void execute_shouldRunOnce_andStoreTheResponse_forNewKey() {
        when(currentUser.requireId()).thenReturn(7L);
        when(idempotencyKeyRepository.insertIfAbsent(eq(7L), eq(KEY), anyString())).thenReturn(1);
        IdempotencyKey stored = IdempotencyKey.builder().userId(7L).idempotencyKey(KEY).requestHash("h").build();
        when(idempotencyKeyRepository.findByUserIdAndIdempotencyKey(7L, KEY)).thenReturn(Optional.of(stored));

        TransferResponse resp = service.execute(KEY, "transfer", request, TransferResponse.class, action);

        assertEquals(100L, resp.getTransactionId());
        assertEquals(1, bookings.get());
        assertNotNull(stored.getResponseBody());
        assertTrue(stored.getResponseBody().contains("\"transactionId\":100"));
    }

    @Test
    void execute_shouldReturnTheStoredResponse_withoutBookingAgain_forRepeatedKey() {
        when(currentUser.requireId()).thenReturn(7L);
        ArgumentCaptor<String> hash = ArgumentCaptor.forClass(String.class);
        when(idempotencyKeyRepository.insertIfAbsent(eq(7L), eq(KEY), hash.capture())).thenReturn(1, 0);
        IdempotencyKey stored = IdempotencyKey.builder().userId(7L).idempotencyKey(KEY).build();
        when(idempotencyKeyRepository.findByUserIdAndIdempotencyKey(7L, KEY)).thenAnswer(invocation -> {
            stored.setRequestHash(hash.getValue());
            return Optional.of(stored);
        });

        TransferResponse first = service.execute(KEY, "transfer", request, TransferResponse.class, action);
        TransferResponse second = service.execute(KEY, "transfer", request, TransferResponse.class, action);

        assertEquals(1, bookings.get());
        assertEquals(first.getTransactionId(), second.getTransactionId());
    }

    @Test
    void execute_shouldReject_whenKeyWasUsedForADifferentRequest() {
        when(currentUser.requireId()).thenReturn(7L);
        when(idempotencyKeyRepository.insertIfAbsent(eq(7L), eq(KEY), anyString())).thenReturn(0);
        when(idempotencyKeyRepository.findByUserIdAndIdempotencyKey(7L, KEY))
                .thenReturn(Optional.of(IdempotencyKey.builder().requestHash("masik-keres").responseBody("{}").build()));

        assertThrows(BusinessException.class, () -> service.execute(KEY, "transfer", request, TransferResponse.class, action));

        assertEquals(0, bookings.get());
    }

    @Test
    void execute_shouldReject_invalidKeyFormat() {
        assertThrows(BusinessException.class, () -> service.execute("rövid", "transfer", request, TransferResponse.class, action));
        assertThrows(BusinessException.class, () -> service.execute("x'; drop table users;--", "transfer", request, TransferResponse.class, action));

        assertEquals(0, bookings.get());
        verifyNoInteractions(idempotencyKeyRepository);
    }
}
