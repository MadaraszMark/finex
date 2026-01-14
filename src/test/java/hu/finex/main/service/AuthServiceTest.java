package hu.finex.main.service;

import hu.finex.main.dto.*;
import hu.finex.main.exception.BusinessException;
import hu.finex.main.exception.NotFoundException;
import hu.finex.main.mapper.UserMapper;
import hu.finex.main.model.LoginLog;
import hu.finex.main.model.User;
import hu.finex.main.model.enums.LoginStatus;
import hu.finex.main.repository.LoginLogRepository;
import hu.finex.main.repository.UserRepository;
import hu.finex.main.security.JwtTokenUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private LoginLogRepository loginLogRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtTokenUtil jwtTokenUtil;
    @Mock private UserMapper userMapper;
    @Mock private AccountService accountService;

    @InjectMocks private AuthService service;

    @Test
    void login_shouldReturnTokenAndUser_andSaveSuccessLoginLog() {
        LoginRequest request = LoginRequest.builder()
                .email("test@example.com")
                .password("pw")
                .build();

        User user = User.builder()
                .id(10L)
                .email("test@example.com")
                .passwordHash("HASH")
                .build();

        when(userRepository.findByEmailIgnoreCase("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("pw", "HASH")).thenReturn(true);
        when(jwtTokenUtil.generateToken("test@example.com")).thenReturn("JWT_TOKEN");

        UserResponse userResponse = UserResponse.builder()
                .id(10L)
                .email("test@example.com")
                .build();
        when(userMapper.toResponse(user)).thenReturn(userResponse);

        AuthResponse resp = service.login(request, "127.0.0.1", "UA");

        assertNotNull(resp);
        assertEquals("JWT_TOKEN", resp.getToken());
        assertNotNull(resp.getUser());
        assertEquals(10L, resp.getUser().getId());
        assertEquals("test@example.com", resp.getUser().getEmail());

        ArgumentCaptor<LoginLog> logCaptor = ArgumentCaptor.forClass(LoginLog.class);
        verify(loginLogRepository).save(logCaptor.capture());
        LoginLog savedLog = logCaptor.getValue();
        assertEquals(user, savedLog.getUser());
        assertEquals(LoginStatus.SUCCESS, savedLog.getStatus());
        assertEquals("127.0.0.1", savedLog.getIpAddress());
        assertEquals("UA", savedLog.getUserAgent());
        assertNull(savedLog.getFailureReason());

        verify(jwtTokenUtil).generateToken("test@example.com");
        verify(userMapper).toResponse(user);
    }

    @Test
    void login_shouldSaveFailedLoginLog_andThrowBusinessException_whenPasswordWrong() {
        LoginRequest request = LoginRequest.builder()
                .email("test@example.com")
                .password("wrong")
                .build();

        User user = User.builder()
                .id(10L)
                .email("test@example.com")
                .passwordHash("HASH")
                .build();

        when(userRepository.findByEmailIgnoreCase("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "HASH")).thenReturn(false);

        assertThrows(BusinessException.class, () -> service.login(request, "10.0.0.1", "UA2"));

        ArgumentCaptor<LoginLog> logCaptor = ArgumentCaptor.forClass(LoginLog.class);
        verify(loginLogRepository).save(logCaptor.capture());
        LoginLog savedLog = logCaptor.getValue();
        assertEquals(user, savedLog.getUser());
        assertEquals(LoginStatus.FAILED, savedLog.getStatus());
        assertEquals("10.0.0.1", savedLog.getIpAddress());
        assertEquals("UA2", savedLog.getUserAgent());
        assertEquals("Hibás jelszó.", savedLog.getFailureReason());

        verifyNoInteractions(jwtTokenUtil, userMapper);
    }

    @Test
    void login_shouldThrowNotFound_andNotSaveLog_whenUserMissing() {
        LoginRequest request = LoginRequest.builder()
                .email("missing@example.com")
                .password("pw")
                .build();

        when(userRepository.findByEmailIgnoreCase("missing@example.com")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.login(request, "1.1.1.1", "UA"));

        verify(userRepository).findByEmailIgnoreCase("missing@example.com");
        verifyNoInteractions(passwordEncoder, loginLogRepository, jwtTokenUtil, userMapper);
    }

    @Test
    void register_shouldThrowBusinessException_whenEmailAlreadyExists() {
        CreateUserRequest request = CreateUserRequest.builder()
                .email("dup@example.com")
                .password("pw")
                .firstName("A")
                .lastName("B")
                .phone("1")
                .role("USER")
                .build();

        when(userRepository.existsByEmailIgnoreCase("dup@example.com")).thenReturn(true);

        assertThrows(BusinessException.class, () -> service.register(request));

        verify(userRepository).existsByEmailIgnoreCase("dup@example.com");
        verifyNoInteractions(passwordEncoder, userMapper, accountService, loginLogRepository, jwtTokenUtil);
        verify(userRepository, never()).save(any());
    }

    @Test
    void register_shouldEncodePassword_saveUser_createDefaultAccount_andReturnResponse() {
        CreateUserRequest request = CreateUserRequest.builder()
                .email("new@example.com")
                .password("pw")
                .firstName("A")
                .lastName("B")
                .phone("1")
                .role("USER")
                .build();

        when(userRepository.existsByEmailIgnoreCase("new@example.com")).thenReturn(false);
        when(passwordEncoder.encode("pw")).thenReturn("HASHED");

        User mapped = User.builder()
                .email("new@example.com")
                .passwordHash("HASHED")
                .build();
        when(userMapper.toEntity(request, "HASHED")).thenReturn(mapped);

        User saved = User.builder()
                .id(55L)
                .email("new@example.com")
                .passwordHash("HASHED")
                .build();
        when(userRepository.save(mapped)).thenReturn(saved);

        UserResponse expected = UserResponse.builder()
                .id(55L)
                .email("new@example.com")
                .build();
        when(userMapper.toResponse(saved)).thenReturn(expected);

        UserResponse resp = service.register(request);

        assertNotNull(resp);
        assertEquals(55L, resp.getId());
        assertEquals("new@example.com", resp.getEmail());

        verify(userRepository).existsByEmailIgnoreCase("new@example.com");
        verify(passwordEncoder).encode("pw");
        verify(userMapper).toEntity(request, "HASHED");
        verify(userRepository).save(mapped);
        verify(accountService).createDefaultAccount(saved);
        verify(userMapper).toResponse(saved);
        verifyNoInteractions(loginLogRepository, jwtTokenUtil);
    }
}
