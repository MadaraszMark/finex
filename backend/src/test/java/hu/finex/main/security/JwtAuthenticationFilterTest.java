package hu.finex.main.security;

import hu.finex.main.model.User;
import hu.finex.main.model.enums.UserRole;
import hu.finex.main.model.enums.UserStatus;
import hu.finex.main.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock private JwtTokenUtil jwtTokenUtil;
    @Mock private UserRepository userRepository;

    @InjectMocks private JwtAuthenticationFilter filter;

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldAuthenticateActiveUser_withUserIdAsPrincipal() throws Exception {
        when(jwtTokenUtil.isValid("TOKEN")).thenReturn(true);
        when(jwtTokenUtil.extractUserId("TOKEN")).thenReturn(42L);
        when(userRepository.findById(42L)).thenReturn(Optional.of(user(UserRole.ADMIN, UserStatus.ACTIVE)));

        MockFilterChain chain = new MockFilterChain();
        filter.doFilter(request("Bearer TOKEN"), new MockHttpServletResponse(), chain);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth);
        assertEquals(42L, auth.getPrincipal());
        assertTrue(auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
        assertNotNull(chain.getRequest());
    }

    @Test
    void shouldNotAuthenticate_whenUserIsBlocked() throws Exception {
        when(jwtTokenUtil.isValid("TOKEN")).thenReturn(true);
        when(jwtTokenUtil.extractUserId("TOKEN")).thenReturn(42L);
        when(userRepository.findById(42L)).thenReturn(Optional.of(user(UserRole.USER, UserStatus.BLOCKED)));

        MockFilterChain chain = new MockFilterChain();
        filter.doFilter(request("Bearer TOKEN"), new MockHttpServletResponse(), chain);

        // A kérés továbbmegy, de hitelesítés nélkül: a védett végpont 401-et ad
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        assertNotNull(chain.getRequest());
    }

    @Test
    void shouldNotAuthenticate_whenTokenIsInvalid() throws Exception {
        when(jwtTokenUtil.isValid("BAD")).thenReturn(false);

        filter.doFilter(request("Bearer BAD"), new MockHttpServletResponse(), new MockFilterChain());

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verifyNoInteractions(userRepository);
    }

    @Test
    void shouldSkip_whenNoBearerHeader() throws Exception {
        filter.doFilter(request(null), new MockHttpServletResponse(), new MockFilterChain());

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verifyNoInteractions(jwtTokenUtil, userRepository);
    }

    private MockHttpServletRequest request(String authorization) {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/accounts");
        if (authorization != null) {
            request.addHeader("Authorization", authorization);
        }
        return request;
    }

    private User user(UserRole role, UserStatus status) {
        return User.builder()
                .id(42L)
                .role(role)
                .status(status)
                .build();
    }
}
