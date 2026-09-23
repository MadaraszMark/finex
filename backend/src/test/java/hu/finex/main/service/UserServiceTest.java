package hu.finex.main.service;

import hu.finex.main.dto.UpdateUserRequest;
import hu.finex.main.dto.UserResponse;
import hu.finex.main.exception.BusinessException;
import hu.finex.main.exception.NotFoundException;
import hu.finex.main.mapper.UserMapper;
import hu.finex.main.model.User;
import hu.finex.main.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private UserMapper userMapper;

    @InjectMocks private UserService service;

    @Test
    void getById_shouldReturnResponse() {
        User user = User.builder().id(1L).email("a@a.com").build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserResponse expected = UserResponse.builder().id(1L).email("a@a.com").build();
        when(userMapper.toResponse(user)).thenReturn(expected);

        UserResponse resp = service.getById(1L);

        assertNotNull(resp);
        assertEquals(1L, resp.getId());
        assertEquals("a@a.com", resp.getEmail());

        verify(userRepository).findById(1L);
        verify(userMapper).toResponse(user);
    }

    @Test
    void getById_shouldThrowNotFound_whenMissing() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.getById(1L));

        verify(userRepository).findById(1L);
        verifyNoInteractions(userMapper);
    }

    @Test
    void update_shouldThrowNotFound_whenUserMissing() {
        UpdateUserRequest req = UpdateUserRequest.builder()
                .email("new@a.com")
                .firstName("A")
                .lastName("B")
                .phone("1")
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.update(1L, req));

        verify(userRepository).findById(1L);
        verifyNoInteractions(userMapper);
        verify(userRepository, never()).save(any());
    }

    @Test
    void update_shouldThrowBusinessException_whenEmailChangedAndAlreadyExists() {
        User user = User.builder().id(1L).email("old@a.com").build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UpdateUserRequest req = UpdateUserRequest.builder()
                .email("new@a.com")
                .firstName("A")
                .lastName("B")
                .phone("1")
                .build();

        when(userRepository.existsByEmailIgnoreCase("new@a.com")).thenReturn(true);

        assertThrows(BusinessException.class, () -> service.update(1L, req));

        verify(userRepository).findById(1L);
        verify(userRepository).existsByEmailIgnoreCase("new@a.com");
        verify(userMapper, never()).updateEntity(any(), any());
        verify(userRepository, never()).save(any());
        verify(userMapper, never()).toResponse(any());
    }

    @Test
    void update_shouldNotCheckEmailExists_whenEmailNotChanged_ignoreCase() {
        User user = User.builder().id(1L).email("User@Example.com").build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UpdateUserRequest req = UpdateUserRequest.builder()
                .email("user@example.com")
                .firstName("A")
                .lastName("B")
                .phone("1")
                .build();

        User saved = User.builder().id(1L).email("user@example.com").build();
        when(userRepository.save(user)).thenReturn(saved);

        UserResponse expected = UserResponse.builder().id(1L).email("user@example.com").build();
        when(userMapper.toResponse(saved)).thenReturn(expected);

        UserResponse resp = service.update(1L, req);

        assertNotNull(resp);
        assertEquals(1L, resp.getId());
        assertEquals("user@example.com", resp.getEmail());

        verify(userRepository).findById(1L);
        verify(userRepository, never()).existsByEmailIgnoreCase(anyString());
        verify(userMapper).updateEntity(user, req);
        verify(userRepository).save(user);
        verify(userMapper).toResponse(saved);
    }

    @Test
    void update_shouldUpdateAndSave_whenEmailChangedAndAvailable() {
        User user = User.builder().id(1L).email("old@a.com").build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UpdateUserRequest req = UpdateUserRequest.builder()
                .email("new@a.com")
                .firstName("A")
                .lastName("B")
                .phone("1")
                .build();

        when(userRepository.existsByEmailIgnoreCase("new@a.com")).thenReturn(false);

        User saved = User.builder().id(1L).email("new@a.com").build();
        when(userRepository.save(user)).thenReturn(saved);

        UserResponse expected = UserResponse.builder().id(1L).email("new@a.com").build();
        when(userMapper.toResponse(saved)).thenReturn(expected);

        UserResponse resp = service.update(1L, req);

        assertNotNull(resp);
        assertEquals(1L, resp.getId());
        assertEquals("new@a.com", resp.getEmail());

        verify(userRepository).findById(1L);
        verify(userRepository).existsByEmailIgnoreCase("new@a.com");
        verify(userMapper).updateEntity(user, req);
        verify(userRepository).save(user);
        verify(userMapper).toResponse(saved);
    }

    @Test
    void delete_shouldThrowNotFound_whenMissing() {
        when(userRepository.existsById(1L)).thenReturn(false);

        assertThrows(NotFoundException.class, () -> service.delete(1L));

        verify(userRepository).existsById(1L);
        verify(userRepository, never()).deleteById(anyLong());
    }

    @Test
    void delete_shouldDeleteById_whenExists() {
        when(userRepository.existsById(1L)).thenReturn(true);

        service.delete(1L);

        verify(userRepository).existsById(1L);
        verify(userRepository).deleteById(1L);
    }

    @Test
    void getOwnProfile_shouldReturnResponse() {
        User user = User.builder().id(2L).email("me@a.com").build();
        when(userRepository.findByEmailIgnoreCase("me@a.com")).thenReturn(Optional.of(user));

        UserResponse expected = UserResponse.builder().id(2L).email("me@a.com").build();
        when(userMapper.toResponse(user)).thenReturn(expected);

        UserResponse resp = service.getOwnProfile("me@a.com");

        assertNotNull(resp);
        assertEquals(2L, resp.getId());
        assertEquals("me@a.com", resp.getEmail());

        verify(userRepository).findByEmailIgnoreCase("me@a.com");
        verify(userMapper).toResponse(user);
    }

    @Test
    void getOwnProfile_shouldThrowNotFound_whenMissing() {
        when(userRepository.findByEmailIgnoreCase("me@a.com")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.getOwnProfile("me@a.com"));

        verify(userRepository).findByEmailIgnoreCase("me@a.com");
        verifyNoInteractions(userMapper);
    }
}
