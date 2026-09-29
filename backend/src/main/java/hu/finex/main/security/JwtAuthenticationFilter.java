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
import hu.finex.main.repository.UserRepository;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenUtil jwtTokenUtil;
    private final UserRepository userRepository;

    public JwtAuthenticationFilter(JwtTokenUtil jwtTokenUtil, UserRepository userRepository) {
        this.jwtTokenUtil = jwtTokenUtil;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,HttpServletResponse response,FilterChain filterChain) throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            if (jwtTokenUtil.isValid(token)) {
                Long userId = jwtTokenUtil.extractUserId(token);

                // A felhasználót minden kérésnél betöltjük: a letiltás és a szerepkör-változás azonnal érvényes lesz,
                // nem csak a token lejárta után. A principal a felhasználó azonosítója (lásd CurrentUser).
                if (userId != null) {
                    userRepository.findById(userId)
                            .filter(user -> user.getStatus() == UserStatus.ACTIVE)
                            .ifPresent(user -> {
                                var authentication = new UsernamePasswordAuthenticationToken(
                                        user.getId(), null, List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())));
                                SecurityContextHolder.getContext().setAuthentication(authentication);
                            });
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}
