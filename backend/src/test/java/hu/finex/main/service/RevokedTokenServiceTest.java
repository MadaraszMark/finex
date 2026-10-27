package hu.finex.main.service;

import hu.finex.main.model.RevokedToken;
import hu.finex.main.repository.RevokedTokenRepository;
import hu.finex.main.security.JwtTokenUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RevokedTokenServiceTest {

    @Mock private RevokedTokenRepository revokedTokenRepository;
    @Mock private JwtTokenUtil jwtTokenUtil;

    @InjectMocks private RevokedTokenService service;

    @Test
    void revoke_shouldStoreTheTokenIdUntilItExpires() {
        Instant expiry = Instant.parse("2026-10-26T21:00:00Z");
        when(jwtTokenUtil.isValid("TOKEN")).thenReturn(true);
        when(jwtTokenUtil.extractTokenId("TOKEN")).thenReturn("jti-1");
        when(jwtTokenUtil.extractExpiration("TOKEN")).thenReturn(expiry);
        when(revokedTokenRepository.existsById("jti-1")).thenReturn(false);

        service.revoke("TOKEN");

        ArgumentCaptor<RevokedToken> saved = ArgumentCaptor.forClass(RevokedToken.class);
        verify(revokedTokenRepository).save(saved.capture());
        assertEquals("jti-1", saved.getValue().getJti());
        assertEquals(expiry, saved.getValue().getExpiresAt());
    }

    @Test
    void revoke_shouldIgnoreInvalidOrMissingToken() {
        when(jwtTokenUtil.isValid("BAD")).thenReturn(false);

        service.revoke("BAD");
        service.revoke(null);

        verifyNoInteractions(revokedTokenRepository);
    }

    @Test
    void isRevoked_shouldCheckTheRepository() {
        when(revokedTokenRepository.existsById("jti-1")).thenReturn(true);

        assertTrue(service.isRevoked("jti-1"));
        assertFalse(service.isRevoked(null));
    }
}
