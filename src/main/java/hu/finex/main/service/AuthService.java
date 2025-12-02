package hu.finex.main.service;

import hu.finex.main.dto.AuthResponse;
import hu.finex.main.dto.CreateUserRequest;
import hu.finex.main.dto.LoginRequest;
import hu.finex.main.dto.UserResponse;
import hu.finex.main.exception.BusinessException;
import hu.finex.main.exception.NotFoundException;
import hu.finex.main.mapper.UserMapper;
import hu.finex.main.model.LoginLog;
import hu.finex.main.model.User;
import hu.finex.main.model.enums.LoginStatus;
import hu.finex.main.repository.LoginLogRepository;
import hu.finex.main.repository.UserRepository;
import hu.finex.main.security.JwtTokenUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final LoginLogRepository loginLogRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenUtil jwtTokenUtil;
    private final UserMapper userMapper;

    @Transactional
    public AuthResponse login(LoginRequest request, String ip, String userAgent) {
        User user = userRepository.findByEmailIgnoreCase(request.getEmail()).orElseThrow(() -> new NotFoundException("Felhasználó nem található."));

        boolean passwordOk = passwordEncoder.matches(request.getPassword(), user.getPasswordHash());

        LoginStatus status = passwordOk ? LoginStatus.SUCCESS : LoginStatus.FAILED;
        String failureReason = passwordOk ? null : "Hibás jelszó.";

        LoginLog log = LoginLog.builder()
                .user(user)
                .status(status)
                .ipAddress(ip)
                .userAgent(userAgent)
                .failureReason(failureReason)
                .build();

        loginLogRepository.save(log);

        if (!passwordOk) {
            throw new BusinessException("Hibás email vagy jelszó.");
        }

        String token = jwtTokenUtil.generateToken(user.getEmail());
        UserResponse userResponse = userMapper.toResponse(user);

        return AuthResponse.builder()
                .token(token)
                .user(userResponse)
                .build();
    }

    @Transactional
    public UserResponse register(CreateUserRequest request) {
        if (userRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw new BusinessException("Ezzel az email címmel már létezik felhasználó.");
        }

        String passwordHash = passwordEncoder.encode(request.getPassword());
        User user = userMapper.toEntity(request, passwordHash);
        user = userRepository.save(user);

        return userMapper.toResponse(user);
    }
}

