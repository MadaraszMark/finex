package hu.finex.main.service;

import hu.finex.main.dto.*;
import hu.finex.main.exception.BusinessException;
import hu.finex.main.exception.NotFoundException;
import hu.finex.main.mapper.AccountMapper;
import hu.finex.main.mapper.UserMapper;
import hu.finex.main.model.Account;
import hu.finex.main.model.User;
import hu.finex.main.model.enums.NotificationType;
import hu.finex.main.model.enums.UserRole;
import hu.finex.main.model.enums.UserStatus;
import hu.finex.main.repository.AccountRepository;
import hu.finex.main.repository.UserRepository;
import hu.finex.main.security.CurrentUser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private UserMapper userMapper;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private CurrentUser currentUser;
    @Mock private NotificationService notificationService;
    @Mock private AccountRepository accountRepository;
    @Mock private AccountMapper accountMapper;
    @Mock private CardService cardService;
    @Mock private SavingsAccountService savingsAccountService;

    @InjectMocks private UserService service;

    @Test
    void getOwnProfile_shouldReturnResponse() {
        User user = User.builder().id(5L).email("me@example.com").build();
        when(currentUser.requireEntity()).thenReturn(user);
        when(userMapper.toResponse(user)).thenReturn(UserResponse.builder().id(5L).email("me@example.com").build());

        UserResponse resp = service.getOwnProfile();

        assertEquals(5L, resp.getId());
        assertEquals("me@example.com", resp.getEmail());
    }

    @Test
    void updateOwnProfile_shouldThrowBusinessException_whenEmailChangedAndAlreadyExists() {
        User user = User.builder().id(1L).email("old@example.com").build();
        when(currentUser.requireEntity()).thenReturn(user);
        when(userRepository.existsByEmailIgnoreCase("new@example.com")).thenReturn(true);

        UpdateUserRequest req = UpdateUserRequest.builder()
                .firstName("A")
                .lastName("B")
                .email("New@Example.com")
                .build();

        assertThrows(BusinessException.class, () -> service.updateOwnProfile(req));

        verify(userMapper, never()).updateEntity(any(), any());
    }

    @Test
    void updateOwnProfile_shouldNotCheckEmailExists_whenEmailNotChanged_ignoreCase() {
        User user = User.builder().id(1L).email("same@example.com").build();
        when(currentUser.requireEntity()).thenReturn(user);
        when(userMapper.toResponse(user)).thenReturn(UserResponse.builder().id(1L).build());

        UpdateUserRequest req = UpdateUserRequest.builder()
                .firstName("A")
                .lastName("B")
                .email("SAME@example.com")
                .build();

        service.updateOwnProfile(req);

        verify(userRepository, never()).existsByEmailIgnoreCase(any());
        verify(userMapper).updateEntity(user, req);
    }

    @Test
    void updateOwnProfile_shouldStoreEmailInLowercase_whenChangedAndAvailable() {
        User user = User.builder().id(1L).email("old@example.com").build();
        when(currentUser.requireEntity()).thenReturn(user);
        when(userRepository.existsByEmailIgnoreCase("new@example.com")).thenReturn(false);
        when(userMapper.toResponse(user)).thenReturn(UserResponse.builder().id(1L).build());

        UpdateUserRequest req = UpdateUserRequest.builder()
                .firstName("A")
                .lastName("B")
                .email("  New@Example.com ")
                .build();

        service.updateOwnProfile(req);

        assertEquals("new@example.com", req.getEmail());
        verify(userMapper).updateEntity(user, req);
    }

    @Test
    void changePassword_shouldEncodeNewPassword_andNotify() {
        User user = User.builder().id(1L).passwordHash("OLD_HASH").build();
        when(currentUser.requireEntity()).thenReturn(user);
        when(passwordEncoder.matches("Old12345", "OLD_HASH")).thenReturn(true);
        when(passwordEncoder.matches("New12345", "OLD_HASH")).thenReturn(false);
        when(passwordEncoder.encode("New12345")).thenReturn("NEW_HASH");

        ChangePasswordRequest req = ChangePasswordRequest.builder()
                .currentPassword("Old12345")
                .newPassword("New12345")
                .build();

        service.changePassword(req);

        assertEquals("NEW_HASH", user.getPasswordHash());
        verify(notificationService).notify(eq(user), eq(NotificationType.SECURITY), eq("Jelszavad megváltozott"), anyString());
    }

    @Test
    void changePassword_shouldThrowBusinessException_whenCurrentPasswordIsWrong() {
        User user = User.builder().id(1L).passwordHash("OLD_HASH").build();
        when(currentUser.requireEntity()).thenReturn(user);
        when(passwordEncoder.matches("Wrong123", "OLD_HASH")).thenReturn(false);

        ChangePasswordRequest req = ChangePasswordRequest.builder()
                .currentPassword("Wrong123")
                .newPassword("New12345")
                .build();

        assertThrows(BusinessException.class, () -> service.changePassword(req));

        assertEquals("OLD_HASH", user.getPasswordHash());
        verify(passwordEncoder, never()).encode(any());
        verifyNoInteractions(notificationService);
    }

    @Test
    void changePassword_shouldThrowBusinessException_whenNewPasswordIsTheSame() {
        User user = User.builder().id(1L).passwordHash("OLD_HASH").build();
        when(currentUser.requireEntity()).thenReturn(user);
        when(passwordEncoder.matches("Same1234", "OLD_HASH")).thenReturn(true);

        ChangePasswordRequest req = ChangePasswordRequest.builder()
                .currentPassword("Same1234")
                .newPassword("Same1234")
                .build();

        assertThrows(BusinessException.class, () -> service.changePassword(req));

        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void search_shouldBuildLowercaseLikePattern() {
        Pageable pageable = PageRequest.of(0, 20);
        User user = User.builder().id(2L).build();
        when(userRepository.search("%kovács%", pageable)).thenReturn(new PageImpl<>(List.of(user), pageable, 1));
        when(userMapper.toListItem(user)).thenReturn(UserListItemResponse.builder().id(2L).build());

        Page<UserListItemResponse> page = service.search(" Kovács ", pageable);

        assertEquals(1, page.getTotalElements());
        assertEquals(2L, page.getContent().get(0).getId());
    }

    @Test
    void search_shouldListEveryone_whenSearchIsEmpty() {
        Pageable pageable = PageRequest.of(0, 20);
        when(userRepository.search(null, pageable)).thenReturn(Page.empty(pageable));

        Page<UserListItemResponse> page = service.search("  ", pageable);

        assertEquals(0, page.getTotalElements());
    }

    @Test
    void getDetails_shouldCollectAccountsCardsAndSavings() {
        User user = User.builder().id(2L).build();
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        when(userMapper.toResponse(user)).thenReturn(UserResponse.builder().id(2L).build());

        Account account = Account.builder().id(1L).build();
        when(accountRepository.findByUser_IdOrderByCreatedAtAsc(2L)).thenReturn(List.of(account));
        when(accountMapper.toListItem(account)).thenReturn(AccountListItemResponse.builder().id(1L).build());
        when(cardService.listByUser(2L)).thenReturn(List.of(CardResponse.builder().id(3L).build()));
        when(savingsAccountService.listByUser(2L)).thenReturn(List.of(SavingsAccountResponse.builder().id(4L).build()));

        UserDetailsResponse resp = service.getDetails(2L);

        assertEquals(2L, resp.getUser().getId());
        assertEquals(1L, resp.getAccounts().get(0).getId());
        assertEquals(3L, resp.getCards().get(0).getId());
        assertEquals(4L, resp.getSavingsAccounts().get(0).getId());
    }

    @Test
    void getDetails_shouldThrowNotFound_whenMissing() {
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.getDetails(2L));
    }

    @Test
    void updateStatus_shouldBlockOtherUser() {
        when(currentUser.requireId()).thenReturn(1L);

        User user = User.builder().id(2L).status(UserStatus.ACTIVE).build();
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        when(userMapper.toResponse(user)).thenReturn(UserResponse.builder().id(2L).status(UserStatus.BLOCKED).build());

        UserResponse resp = service.updateStatus(2L, UpdateUserStatusRequest.builder().status(UserStatus.BLOCKED).build());

        assertEquals(UserStatus.BLOCKED, user.getStatus());
        assertEquals(UserStatus.BLOCKED, resp.getStatus());
    }

    @Test
    void updateStatus_shouldThrowBusinessException_whenAdminBlocksHimself() {
        when(currentUser.requireId()).thenReturn(1L);

        assertThrows(BusinessException.class, () -> service.updateStatus(1L, UpdateUserStatusRequest.builder().status(UserStatus.BLOCKED).build()));

        verifyNoInteractions(userRepository);
    }

    @Test
    void updateRole_shouldChangeRole_andNotifyUser() {
        when(currentUser.requireId()).thenReturn(1L);

        User user = User.builder().id(2L).role(UserRole.USER).build();
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        when(userMapper.toResponse(user)).thenReturn(UserResponse.builder().id(2L).role(UserRole.ADMIN).build());

        service.updateRole(2L, UpdateUserRoleRequest.builder().role(UserRole.ADMIN).build());

        assertEquals(UserRole.ADMIN, user.getRole());
        verify(notificationService).notify(eq(user), eq(NotificationType.SYSTEM), anyString(), contains("ADMIN"));
    }

    @Test
    void updateRole_shouldThrowBusinessException_whenChangingOwnRole() {
        when(currentUser.requireId()).thenReturn(1L);

        assertThrows(BusinessException.class, () -> service.updateRole(1L, UpdateUserRoleRequest.builder().role(UserRole.USER).build()));

        verifyNoInteractions(userRepository, notificationService);
    }
}
