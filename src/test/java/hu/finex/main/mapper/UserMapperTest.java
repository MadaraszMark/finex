package hu.finex.main.mapper;

import hu.finex.main.dto.CreateUserRequest;
import hu.finex.main.dto.UpdateUserRequest;
import hu.finex.main.dto.UserResponse;
import hu.finex.main.model.User;
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
                .role("USER")
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
        assertEquals("USER", user.getRole());
    }

    @Test
    void testUpdateEntity() {
        User user = User.builder()
                .id(20L)
                .firstName("Old")
                .lastName("Name")
                .email("old@email.com")
                .phone("0000")
                .role("USER")
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
        assertEquals("USER", user.getRole());
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
                .role("ADMIN")
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
        assertEquals("ADMIN", response.getRole());
        assertEquals(createdAt, response.getCreatedAt());
        assertEquals(updatedAt, response.getUpdatedAt());
    }
}
