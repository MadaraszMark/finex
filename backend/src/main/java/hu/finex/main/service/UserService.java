package hu.finex.main.service;

import java.util.Locale;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

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
import hu.finex.main.model.enums.NotificationType;
import hu.finex.main.repository.AccountRepository;
import hu.finex.main.repository.UserRepository;
import hu.finex.main.security.CurrentUser;
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

    @Transactional(readOnly = true)
    public UserResponse getOwnProfile() {
        return userMapper.toResponse(currentUser.requireEntity());
    }

    // Saját adatok módosítása (a token a felhasználó azonosítójához kötött, így e-mail-csere után is érvényes marad)
    @Transactional
    public UserResponse updateOwnProfile(UpdateUserRequest request) {
        User user = currentUser.requireEntity();
        request.setEmail(request.getEmail().trim().toLowerCase(Locale.ROOT));

        boolean emailChanged = !user.getEmail().equalsIgnoreCase(request.getEmail());
        if (emailChanged && userRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw new BusinessException("Ezzel az email címmel már létezik felhasználó.");
        }

        userMapper.updateEntity(user, request);

        return userMapper.toResponse(user);
    }

    // Jelszócsere: a régi jelszót is ellenőrizzük, és biztonsági értesítés megy róla
    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        User user = currentUser.requireEntity();

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new BusinessException("A jelenlegi jelszó hibás.");
        }
        if (passwordEncoder.matches(request.getNewPassword(), user.getPasswordHash())) {
            throw new BusinessException("Az új jelszó nem egyezhet meg a régivel.");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));

        notificationService.notify(user, NotificationType.SECURITY, "Jelszavad megváltozott",
                "Ha nem te változtattad meg, azonnal vedd fel a kapcsolatot az ügyfélszolgálattal.");
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

    // Admin: letiltás / feloldás. A letiltott felhasználó azonnal kijelentkezik (a JwtAuthenticationFilter minden kérésnél ellenőrzi).
    @Transactional
    public UserResponse updateStatus(Long id, UpdateUserStatusRequest request) {
        if (id.equals(currentUser.requireId())) {
            throw new BusinessException("A saját fiókodat nem tilthatod le.");
        }

        User user = findById(id);
        user.setStatus(request.getStatus());

        return userMapper.toResponse(user);
    }

    // Admin: szerepkör módosítása (a saját szerepkör nem módosítható, hogy ne maradjon admin nélkül a rendszer)
    @Transactional
    public UserResponse updateRole(Long id, UpdateUserRoleRequest request) {
        if (id.equals(currentUser.requireId())) {
            throw new BusinessException("A saját szerepkörödet nem módosíthatod.");
        }

        User user = findById(id);
        user.setRole(request.getRole());

        notificationService.notify(user, NotificationType.SYSTEM, "Szerepköröd megváltozott",
                "Új szerepkör: " + request.getRole().name() + ". A változás a következő kéréstől érvényes.");

        return userMapper.toResponse(user);
    }

    private User findById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new NotFoundException("Felhasználó nem található."));
    }
}
