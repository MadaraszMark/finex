package hu.finex.main.security;

import java.io.IOException;
import java.util.List;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import hu.finex.main.model.enums.UserStatus;
import hu.finex.main.repository.RevokedTokenRepository;
import hu.finex.main.repository.UserRepository;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenUtil jwtTokenUtil;
    private final UserRepository userRepository;
    private final RevokedTokenRepository revokedTokenRepository;

    public JwtAuthenticationFilter(JwtTokenUtil jwtTokenUtil, UserRepository userRepository, RevokedTokenRepository revokedTokenRepository) {
        this.jwtTokenUtil = jwtTokenUtil;
        this.userRepository = userRepository;
        this.revokedTokenRepository = revokedTokenRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,HttpServletResponse response,FilterChain filterChain) throws ServletException, IOException {

        String token = extractToken(request);

        if (token != null && jwtTokenUtil.isValid(token)) {
            Long userId = jwtTokenUtil.extractUserId(token);
            Integer tokenVersion = jwtTokenUtil.extractTokenVersion(token);
            String tokenId = jwtTokenUtil.extractTokenId(token);

            // A felhasználót minden kérésnél betöltjük, így a letiltás és a szerepkör-változás azonnal érvényes
            if (userId != null && tokenVersion != null && tokenId != null && !revokedTokenRepository.existsById(tokenId)) {
                userRepository.findById(userId)
                        .filter(user -> user.getStatus() == UserStatus.ACTIVE)
                        .filter(user -> user.getTokenVersion() == tokenVersion)
                        .ifPresent(user -> {
                            var authentication = new UsernamePasswordAuthenticationToken(
                                    user.getId(), null, List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())));
                            SecurityContextHolder.getContext().setAuthentication(authentication);
                        });
            }
        }

        filterChain.doFilter(request, response);
    }

    // A "Bearer <token>" fejlécből a token (a kijelentkezés végpontja is ezt használja)
    public static String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        return header != null && header.startsWith("Bearer ") ? header.substring(7) : null;
    }
}
