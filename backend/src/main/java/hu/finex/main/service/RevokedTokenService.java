package hu.finex.main.service;

import java.time.Instant;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hu.finex.main.model.RevokedToken;
import hu.finex.main.repository.RevokedTokenRepository;
import hu.finex.main.security.JwtTokenUtil;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RevokedTokenService {

    private final RevokedTokenRepository revokedTokenRepository;
    private final JwtTokenUtil jwtTokenUtil;

    // Kijelentkezés: a token a lejáratáig a visszavontak között marad
    @Transactional
    public void revoke(String token) {
        if (!jwtTokenUtil.isValid(token)) {
            return;
        }

        String jti = jwtTokenUtil.extractTokenId(token);
        if (jti != null && !revokedTokenRepository.existsById(jti)) {
            revokedTokenRepository.save(RevokedToken.builder().jti(jti).expiresAt(jwtTokenUtil.extractExpiration(token)).build());
        }
    }

    @Transactional(readOnly = true)
    public boolean isRevoked(String jti) {
        return jti != null && revokedTokenRepository.existsById(jti);
    }

    // A lejárt tokenek sorainak törlése (az ütemező hívja óránként)
    @Transactional
    public int deleteExpired() {
        return revokedTokenRepository.deleteExpired(Instant.now());
    }
}
