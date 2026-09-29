package hu.finex.main.mapper;

import hu.finex.main.dto.CreateUserRequest;
import hu.finex.main.dto.UpdateUserRequest;
import hu.finex.main.dto.UserListItemResponse;
import hu.finex.main.dto.UserResponse;
import hu.finex.main.model.User;
import hu.finex.main.model.enums.UserRole;
import hu.finex.main.model.enums.UserStatus;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class UserMapperTest {

    private final UserMapper mapper = new UserMapper();

    @Test
    void testToEntity() {
        CreateUserRequest request = CreateUserRequest.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .phone("+3612345678")
                .password("Secret123")
                .build();

        String passwordHash = "$2a$10$hashedpassword";

        User user = mapper.toEntity(request, passwordHash);

        assertNotNull(user);
        assertNull(user.getId());
        assertEquals("John", user.getFirstName());
        assertEquals("Doe", user.getLastName());
        assertEquals("john.doe@example.com", user.getEmail());
        assertEquals("+3612345678", user.getPhone());
        assertEquals(passwordHash, user.getPasswordHash());
        assertEquals(UserRole.USER, user.getRole());
        assertEquals(UserStatus.ACTIVE, user.getStatus());
    }

    @Test
    void testUpdateEntity() {
        User user = User.builder()
                .id(20L)
                .firstName("Old")
                .lastName("Name")
                .email("old@email.com")
                .phone("0000")
                .role(UserRole.USER)
                .build();

        UpdateUserRequest request = UpdateUserRequest.builder()
                .firstName("New")
                .lastName("Name")
                .email("new@email.com")
                .phone("1111")
                .build();

        mapper.updateEntity(user, request);

        assertEquals(20L, user.getId());
        assertEquals("New", user.getFirstName());
        assertEquals("Name", user.getLastName());
        assertEquals("new@email.com", user.getEmail());
        assertEquals("1111", user.getPhone());
        assertEquals(UserRole.USER, user.getRole());
    }

    @Test
    void testToResponse() {
        Instant createdAt = Instant.parse("2025-01-01T09:00:00Z");
        Instant updatedAt = Instant.parse("2025-01-05T14:00:00Z");

        User user = User.builder()
                .id(7L)
                .firstName("Anna")
                .lastName("Kovács")
                .email("anna.kovacs@example.com")
                .phone("+36301234567")
                .role(UserRole.ADMIN)
                .status(UserStatus.ACTIVE)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();

        UserResponse response = mapper.toResponse(user);

        assertNotNull(response);
        assertEquals(7L, response.getId());
        assertEquals("Anna", response.getFirstName());
        assertEquals("Kovács", response.getLastName());
        assertEquals("anna.kovacs@example.com", response.getEmail());
        assertEquals("+36301234567", response.getPhone());
        assertEquals(UserRole.ADMIN, response.getRole());
        assertEquals(UserStatus.ACTIVE, response.getStatus());
        assertEquals(createdAt, response.getCreatedAt());
        assertEquals(updatedAt, response.getUpdatedAt());
    }

    @Test
    void testToListItem() {
        Instant createdAt = Instant.parse("2025-02-01T08:00:00Z");

        User user = User.builder()
                .id(8L)
                .firstName("Bence")
                .lastName("Nagy")
                .email("bence@finex.hu")
                .role(UserRole.USER)
                .status(UserStatus.BLOCKED)
                .createdAt(createdAt)
                .build();

        UserListItemResponse response = mapper.toListItem(user);

        assertNotNull(response);
        assertEquals(8L, response.getId());
        assertEquals("Nagy Bence", response.getFullName());
        assertEquals("bence@finex.hu", response.getEmail());
        assertEquals(UserRole.USER, response.getRole());
        assertEquals(UserStatus.BLOCKED, response.getStatus());
        assertEquals(createdAt, response.getCreatedAt());
    }
}
