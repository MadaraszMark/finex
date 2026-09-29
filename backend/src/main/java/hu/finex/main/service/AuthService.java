package hu.finex.main.service;

import java.util.Locale;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hu.finex.main.config.FinexProperties;
import hu.finex.main.dto.AuthResponse;
import hu.finex.main.dto.CreateUserRequest;
import hu.finex.main.dto.LoginRequest;
import hu.finex.main.exception.BusinessException;
import hu.finex.main.mapper.UserMapper;
import hu.finex.main.model.User;
import hu.finex.main.model.enums.LoginStatus;
import hu.finex.main.model.enums.NotificationType;
import hu.finex.main.model.enums.UserStatus;
import hu.finex.main.repository.UserRepository;
import hu.finex.main.security.JwtTokenUtil;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    // Egységes üzenet: kívülről ne lehessen kideríteni, hogy egy e-mail cím regisztrálva van-e
    private static final String INVALID_CREDENTIALS = "Hibás email vagy jelszó.";

    private final UserRepository userRepository;
    private final LoginLogService loginLogService;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenUtil jwtTokenUtil;
    private final UserMapper userMapper;
    private final AccountService accountService;
    private final NotificationService notificationService;
    private final FinexProperties finexProperties;

    // Szándékosan nincs @Transactional: minden próbálkozás a LoginLogService saját tranzakciójában naplózódik,
    // így a sikertelen belépés naplója a kivétel után is megmarad
    public AuthResponse login(LoginRequest request, String ip, String userAgent) {
        String email = normalizeEmail(request.getEmail());
        User user = userRepository.findByEmailIgnoreCase(email).orElse(null);

        // Túl sok sikertelen próbálkozás után átmenetileg zárolva (brute force elleni védelem)
        if (loginLogService.isTemporarilyLocked(email)) {
            loginLogService.recordAttempt(user, email, LoginStatus.FAILED, ip, userAgent, "Átmenetileg zárolva");
            throw new BusinessException("Túl sok sikertelen bejelentkezési kísérlet. Próbáld újra " + finexProperties.getLoginLockMinutes() + " perc múlva.");
        }

        if (user == null) {
            loginLogService.recordAttempt(null, email, LoginStatus.FAILED, ip, userAgent, "Ismeretlen e-mail cím");
            throw new BusinessException(INVALID_CREDENTIALS);
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            loginLogService.recordAttempt(user, email, LoginStatus.FAILED, ip, userAgent, "Hibás jelszó");
            notifyIfJustLocked(user, email);
            throw new BusinessException(INVALID_CREDENTIALS);
        }

        if (user.getStatus() == UserStatus.BLOCKED) {
            loginLogService.recordAttempt(user, email, LoginStatus.FAILED, ip, userAgent, "Letiltott felhasználó");
            throw new BusinessException("A fiókod le van tiltva. Keresd az ügyfélszolgálatot.");
        }

        loginLogService.recordAttempt(user, email, LoginStatus.SUCCESS, ip, userAgent, null);

        return buildAuthResponse(user);
    }

    @Transactional
    public AuthResponse register(CreateUserRequest request) {
        request.setEmail(normalizeEmail(request.getEmail()));

        if (userRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw new BusinessException("Ezzel az email címmel már létezik felhasználó.");
        }

        String passwordHash = passwordEncoder.encode(request.getPassword());
        User user = userMapper.toEntity(request, passwordHash);
        user = userRepository.save(user);

        // Regisztráció után automatikusan létrejön az alapértelmezett folyószámla és hozzá egy bankkártya
        accountService.createDefaultAccount(user);

        notificationService.notify(user, NotificationType.SYSTEM, "Üdvözlünk a FineX-ben!", "A folyószámládat és a bankkártyádat már használhatod.");

        // Regisztráció után rögtön be is van jelentkezve
        return buildAuthResponse(user);
    }

    // Ha éppen ez a próbálkozás zárolta a fiókot, a felhasználó biztonsági értesítést kap
    private void notifyIfJustLocked(User user, String email) {
        if (loginLogService.isTemporarilyLocked(email)) {
            notificationService.notify(user, NotificationType.SECURITY, "Fiókod átmenetileg zárolva",
                    "Túl sok sikertelen bejelentkezési kísérlet történt, ezért a fiókod " + finexProperties.getLoginLockMinutes()
                    + " percre zárolva lett. Ha nem te voltál, változtass jelszót.");
        }
    }

    private AuthResponse buildAuthResponse(User user) {
        String token = jwtTokenUtil.generateToken(user);

        return AuthResponse.builder()
                .token(token)
                .expiresAt(jwtTokenUtil.extractExpiration(token))
                .user(userMapper.toResponse(user))
                .build();
    }

    // Az e-mail címet mindig kisbetűsen tároljuk és keressük (az adatbázis CHECK szabálya is ezt követeli meg)
    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
