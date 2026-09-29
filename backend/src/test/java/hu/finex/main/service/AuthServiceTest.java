package hu.finex.main.service;

import hu.finex.main.config.FinexProperties;
import hu.finex.main.dto.*;
import hu.finex.main.exception.BusinessException;
import hu.finex.main.mapper.UserMapper;
import hu.finex.main.model.User;
import hu.finex.main.model.enums.LoginStatus;
import hu.finex.main.model.enums.NotificationType;
import hu.finex.main.model.enums.UserStatus;
import hu.finex.main.repository.UserRepository;
import hu.finex.main.security.JwtTokenUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private LoginLogService loginLogService;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtTokenUtil jwtTokenUtil;
    @Mock private UserMapper userMapper;
    @Mock private AccountService accountService;
    @Mock private NotificationService notificationService;
    @Spy private FinexProperties finexProperties = new FinexProperties();

    @InjectMocks private AuthService service;

    @Test
    void login_shouldReturnTokenAndUser_andRecordSuccess() {
        LoginRequest request = LoginRequest.builder()
                .email("  Test@Example.com ")
                .password("pw")
                .build();

        User user = activeUser();

        when(userRepository.findByEmailIgnoreCase("test@example.com")).thenReturn(Optional.of(user));
        when(loginLogService.isTemporarilyLocked("test@example.com")).thenReturn(false);
        when(passwordEncoder.matches("pw", "HASH")).thenReturn(true);
        when(jwtTokenUtil.generateToken(user)).thenReturn("JWT_TOKEN");

        Instant expiresAt = Instant.parse("2025-03-10T11:00:00Z");
        when(jwtTokenUtil.extractExpiration("JWT_TOKEN")).thenReturn(expiresAt);

        UserResponse userResponse = UserResponse.builder()
                .id(10L)
                .email("test@example.com")
                .build();
        when(userMapper.toResponse(user)).thenReturn(userResponse);

        AuthResponse resp = service.login(request, "127.0.0.1", "UA");

        assertNotNull(resp);
        assertEquals("JWT_TOKEN", resp.getToken());
        assertEquals(expiresAt, resp.getExpiresAt());
        assertEquals(10L, resp.getUser().getId());

        verify(loginLogService).recordAttempt(user, "test@example.com", LoginStatus.SUCCESS, "127.0.0.1", "UA", null);
    }

    @Test
    void login_shouldRecordFailure_andThrowBusinessException_whenPasswordWrong() {
        LoginRequest request = LoginRequest.builder()
                .email("test@example.com")
                .password("bad")
                .build();

        User user = activeUser();

        when(userRepository.findByEmailIgnoreCase("test@example.com")).thenReturn(Optional.of(user));
        when(loginLogService.isTemporarilyLocked("test@example.com")).thenReturn(false);
        when(passwordEncoder.matches("bad", "HASH")).thenReturn(false);

        BusinessException ex = assertThrows(BusinessException.class, () -> service.login(request, "127.0.0.1", "UA"));

        assertEquals("Hibás email vagy jelszó.", ex.getMessage());
        verify(loginLogService).recordAttempt(user, "test@example.com", LoginStatus.FAILED, "127.0.0.1", "UA", "Hibás jelszó");
        verifyNoInteractions(jwtTokenUtil, notificationService);
    }

    @Test
    void login_shouldNotifyUser_whenThisAttemptLockedTheAccount() {
        LoginRequest request = LoginRequest.builder()
                .email("test@example.com")
                .password("bad")
                .build();

        User user = activeUser();

        when(userRepository.findByEmailIgnoreCase("test@example.com")).thenReturn(Optional.of(user));
        // Az ellenőrzéskor még nincs zárolva, a mostani hibás próbálkozás után már igen
        when(loginLogService.isTemporarilyLocked("test@example.com")).thenReturn(false, true);
        when(passwordEncoder.matches("bad", "HASH")).thenReturn(false);

        assertThrows(BusinessException.class, () -> service.login(request, "127.0.0.1", "UA"));

        verify(notificationService).notify(eq(user), eq(NotificationType.SECURITY), eq("Fiókod átmenetileg zárolva"), anyString());
    }

    @Test
    void login_shouldRecordFailure_withSameMessage_whenEmailUnknown() {
        LoginRequest request = LoginRequest.builder()
                .email("missing@example.com")
                .password("pw")
                .build();

        when(userRepository.findByEmailIgnoreCase("missing@example.com")).thenReturn(Optional.empty());
        when(loginLogService.isTemporarilyLocked("missing@example.com")).thenReturn(false);

        BusinessException ex = assertThrows(BusinessException.class, () -> service.login(request, "127.0.0.1", "UA"));

        // Ugyanaz az üzenet, mint hibás jelszónál: kívülről nem derül ki, hogy létezik-e a cím
        assertEquals("Hibás email vagy jelszó.", ex.getMessage());
        verify(loginLogService).recordAttempt(null, "missing@example.com", LoginStatus.FAILED, "127.0.0.1", "UA", "Ismeretlen e-mail cím");
        verifyNoInteractions(passwordEncoder, jwtTokenUtil);
    }

    @Test
    void login_shouldThrowBusinessException_withoutCheckingPassword_whenTemporarilyLocked() {
        LoginRequest request = LoginRequest.builder()
                .email("test@example.com")
                .password("pw")
                .build();

        User user = activeUser();

        when(userRepository.findByEmailIgnoreCase("test@example.com")).thenReturn(Optional.of(user));
        when(loginLogService.isTemporarilyLocked("test@example.com")).thenReturn(true);

        BusinessException ex = assertThrows(BusinessException.class, () -> service.login(request, "127.0.0.1", "UA"));

        assertTrue(ex.getMessage().contains("15 perc"));
        verify(loginLogService).recordAttempt(user, "test@example.com", LoginStatus.FAILED, "127.0.0.1", "UA", "Átmenetileg zárolva");
        verifyNoInteractions(passwordEncoder, jwtTokenUtil);
    }

    @Test
    void login_shouldThrowBusinessException_whenUserIsBlocked() {
        LoginRequest request = LoginRequest.builder()
                .email("test@example.com")
                .password("pw")
                .build();

        User user = activeUser();
        user.setStatus(UserStatus.BLOCKED);

        when(userRepository.findByEmailIgnoreCase("test@example.com")).thenReturn(Optional.of(user));
        when(loginLogService.isTemporarilyLocked("test@example.com")).thenReturn(false);
        when(passwordEncoder.matches("pw", "HASH")).thenReturn(true);

        assertThrows(BusinessException.class, () -> service.login(request, "127.0.0.1", "UA"));

        verify(loginLogService).recordAttempt(user, "test@example.com", LoginStatus.FAILED, "127.0.0.1", "UA", "Letiltott felhasználó");
        verifyNoInteractions(jwtTokenUtil);
    }

    @Test
    void register_shouldThrowBusinessException_whenEmailAlreadyExists() {
        CreateUserRequest request = CreateUserRequest.builder()
                .firstName("Test")
                .lastName("User")
                .email("Exists@Example.com")
                .password("Secret123")
                .build();

        when(userRepository.existsByEmailIgnoreCase("exists@example.com")).thenReturn(true);

        assertThrows(BusinessException.class, () -> service.register(request));

        verify(userRepository, never()).save(any());
        verifyNoInteractions(passwordEncoder, accountService, jwtTokenUtil);
    }

    @Test
    void register_shouldEncodePassword_createDefaultAccount_notify_andLogIn() {
        CreateUserRequest request = CreateUserRequest.builder()
                .firstName("Anna")
                .lastName("Kovács")
                .email(" Anna@Finex.hu")
                .password("Secret123")
                .build();

        when(userRepository.existsByEmailIgnoreCase("anna@finex.hu")).thenReturn(false);
        when(passwordEncoder.encode("Secret123")).thenReturn("ENCODED");

        User mapped = User.builder().email("anna@finex.hu").passwordHash("ENCODED").build();
        when(userMapper.toEntity(request, "ENCODED")).thenReturn(mapped);

        User saved = User.builder().id(20L).email("anna@finex.hu").passwordHash("ENCODED").build();
        when(userRepository.save(mapped)).thenReturn(saved);

        when(jwtTokenUtil.generateToken(saved)).thenReturn("JWT_TOKEN");
        when(userMapper.toResponse(saved)).thenReturn(UserResponse.builder().id(20L).email("anna@finex.hu").build());

        AuthResponse resp = service.register(request);

        assertEquals("JWT_TOKEN", resp.getToken());
        assertEquals(20L, resp.getUser().getId());
        assertEquals("anna@finex.hu", request.getEmail());

        verify(accountService).createDefaultAccount(saved);
        verify(notificationService).notify(eq(saved), eq(NotificationType.SYSTEM), anyString(), anyString());
    }

    private User activeUser() {
        return User.builder()
                .id(10L)
                .email("test@example.com")
                .passwordHash("HASH")
                .status(UserStatus.ACTIVE)
                .build();
    }
}
