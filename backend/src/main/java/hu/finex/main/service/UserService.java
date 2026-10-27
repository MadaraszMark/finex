package hu.finex.main.service;

import java.util.Locale;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import hu.finex.main.dto.AuthResponse;
import hu.finex.main.dto.ChangePasswordRequest;
import hu.finex.main.dto.UpdateUserRequest;
import hu.finex.main.dto.UpdateUserRoleRequest;
import hu.finex.main.dto.UpdateUserStatusRequest;
import hu.finex.main.dto.UserDetailsResponse;
import hu.finex.main.dto.UserListItemResponse;
import hu.finex.main.dto.UserResponse;
import hu.finex.main.exception.BusinessException;
import hu.finex.main.exception.NotFoundException;
import hu.finex.main.mapper.AccountMapper;
import hu.finex.main.mapper.UserMapper;
import hu.finex.main.model.User;
import hu.finex.main.model.enums.AuditAction;
import hu.finex.main.model.enums.AuditTargetType;
import hu.finex.main.model.enums.NotificationType;
import hu.finex.main.model.enums.UserRole;
import hu.finex.main.model.enums.UserStatus;
import hu.finex.main.repository.AccountRepository;
import hu.finex.main.repository.UserRepository;
import hu.finex.main.security.CurrentUser;
import hu.finex.main.util.PasswordPolicy;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final CurrentUser currentUser;
    private final NotificationService notificationService;
    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;
    private final CardService cardService;
    private final SavingsAccountService savingsAccountService;
    private final AuthService authService;
    private final AuditService auditService;

    @Transactional(readOnly = true)
    public UserResponse getOwnProfile() {
        return userMapper.toResponse(currentUser.requireEntity());
    }

    // Saját adatok módosítása (a token az azonosítóhoz kötött, így e-mail-csere után is érvényes)
    @Transactional
    public UserResponse updateOwnProfile(UpdateUserRequest request) {
        User user = currentUser.requireEntity();
        request.setEmail(request.getEmail().trim().toLowerCase(Locale.ROOT));

        boolean emailChanged = !user.getEmail().equalsIgnoreCase(request.getEmail());
        if (emailChanged) {
            // Az e-mail cseréjéhez jelszó kell, különben egy ellopott tokennel átvehető lenne a fiók
            if (!PasswordPolicy.fitsBcrypt(request.getCurrentPassword()) || !passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
                throw new BusinessException("Az e-mail cím módosításához add meg a jelenlegi jelszavadat.");
            }
            if (userRepository.existsByEmailIgnoreCase(request.getEmail())) {
                throw new BusinessException("Ezzel az email címmel már létezik felhasználó.");
            }
        }

        String previousEmail = user.getEmail();
        userMapper.updateEntity(user, request);

        if (emailChanged) {
            notificationService.notify(user, NotificationType.SECURITY, "E-mail címed megváltozott",
                    "A bejelentkezési e-mail címed mostantól " + user.getEmail() + " (korábban: " + previousEmail + "). Ha nem te voltál, azonnal keresd az ügyfélszolgálatot.");
        }

        return userMapper.toResponse(user);
    }

    // Jelszócsere: a régi tokenek érvénytelenné válnak, az aktuális munkamenet új tokent kap
    @Transactional
    public AuthResponse changePassword(ChangePasswordRequest request) {
        User user = currentUser.requireEntity();

        if (!PasswordPolicy.fitsBcrypt(request.getCurrentPassword()) || !passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new BusinessException("A jelenlegi jelszó hibás.");
        }
        if (!PasswordPolicy.fitsBcrypt(request.getNewPassword())) {
            throw new BusinessException(PasswordPolicy.TOO_LONG_MESSAGE);
        }
        if (passwordEncoder.matches(request.getNewPassword(), user.getPasswordHash())) {
            throw new BusinessException("Az új jelszó nem egyezhet meg a régivel.");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.revokeTokens();

        notificationService.notify(user, NotificationType.SECURITY, "Jelszavad megváltozott",
                "Minden más eszközön kijelentkeztettünk. Ha nem te változtattad meg, azonnal vedd fel a kapcsolatot az ügyfélszolgálattal.");

        return authService.issueToken(user);
    }

    // Kijelentkezés minden eszközről: a régi tokenek érvénytelenek, ez a munkamenet új tokent kap
    @Transactional
    public AuthResponse logoutEverywhere() {
        User user = currentUser.requireEntity();
        user.revokeTokens();

        notificationService.notify(user, NotificationType.SECURITY, "Kijelentkeztettünk minden eszközről",
                "Minden más eszközön lejárt a bejelentkezésed. Ha nem te kérted, változtass jelszót.");

        return authService.issueToken(user);
    }

    // Admin: felhasználók keresése névre vagy e-mail címre
    @Transactional(readOnly = true)
    public Page<UserListItemResponse> search(String search, Pageable pageable) {
        String pattern = StringUtils.hasText(search) ? "%" + search.trim().toLowerCase(Locale.ROOT) + "%" : null;

        return userRepository.search(pattern, pageable).map(userMapper::toListItem);
    }

    // Admin: egy felhasználó teljes áttekintése (számlák, kártyák, megtakarítások)
    @Transactional(readOnly = true)
    public UserDetailsResponse getDetails(Long id) {
        User user = findById(id);

        return UserDetailsResponse.builder()
                .user(userMapper.toResponse(user))
                .accounts(accountRepository.findByUser_IdOrderByCreatedAtAsc(id).stream().map(accountMapper::toListItem).toList())
                .cards(cardService.listByUser(id))
                .savingsAccounts(savingsAccountService.listByUser(id))
                .build();
    }

    // Admin: letiltás / feloldás; a tokenek visszavonódnak, így feloldás után sem él egy régi token
    @Transactional
    public UserResponse updateStatus(Long id, UpdateUserStatusRequest request) {
        if (id.equals(currentUser.requireId())) {
            throw new BusinessException("A saját fiókodat nem tilthatod le.");
        }

        User user = findById(id);
        UserStatus previous = user.getStatus();
        user.setStatus(request.getStatus());
        user.revokeTokens();

        auditService.record(AuditAction.USER_STATUS_CHANGED, AuditTargetType.USER, user.getId(),
                user.getEmail() + ": " + previous + " → " + request.getStatus());

        return userMapper.toResponse(user);
    }

    // Admin: szerepkör módosítása (a sajátját nem), a tokenek visszavonódnak, így újra be kell lépni
    @Transactional
    public UserResponse updateRole(Long id, UpdateUserRoleRequest request) {
        if (id.equals(currentUser.requireId())) {
            throw new BusinessException("A saját szerepkörödet nem módosíthatod.");
        }

        User user = findById(id);
        UserRole previous = user.getRole();
        user.setRole(request.getRole());
        user.revokeTokens();

        auditService.record(AuditAction.USER_ROLE_CHANGED, AuditTargetType.USER, user.getId(),
                user.getEmail() + ": " + previous + " → " + request.getRole());

        notificationService.notify(user, NotificationType.SYSTEM, "Szerepköröd megváltozott",
                "Új szerepkör: " + request.getRole().name() + ". A változás a következő kéréstől érvényes.");

        return userMapper.toResponse(user);
    }

    private User findById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new NotFoundException("Felhasználó nem található."));
    }
}
