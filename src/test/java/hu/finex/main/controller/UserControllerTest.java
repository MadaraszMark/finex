package hu.finex.main.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import hu.finex.main.dto.UpdateUserRequest;
import hu.finex.main.dto.UserResponse;
import hu.finex.main.service.UserService;

@ActiveProfiles("test")
@WebMvcTest(
    controllers = UserController.class,
    excludeFilters = {
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = hu.finex.main.config.SecurityConfig.class),
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = hu.finex.main.security.JwtAuthenticationFilter.class)
    }
)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockBean UserService userService;

    private UserResponse sampleUser() {
        return UserResponse.builder()
                .id(42L)
                .firstName("Bence")
                .lastName("Kovács")
                .email("bence.kovacs@example.com")
                .phone("+36301234567")
                .role("USER")
                .createdAt(Instant.parse("2025-02-12T14:22:10Z"))
                .updatedAt(Instant.parse("2025-02-13T11:01:45Z"))
                .build();
    }


    @Test
    void getById_shouldReturn200_andBody() throws Exception {
        when(userService.getById(42L)).thenReturn(sampleUser());

        mockMvc.perform(get("/users/42"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(42))
                .andExpect(jsonPath("$.firstName").value("Bence"))
                .andExpect(jsonPath("$.lastName").value("Kovács"))
                .andExpect(jsonPath("$.email").value("bence.kovacs@example.com"))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    void update_shouldReturn200_andBody() throws Exception {
        UpdateUserRequest req = UpdateUserRequest.builder()
                .firstName("Bence")
                .lastName("Kovács")
                .email("bence.kovacs@example.com")
                .phone("+36309998877")
                .build();

        UserResponse updated = sampleUser();
        updated.setPhone("+36309998877");

        when(userService.update(eq(42L), any(UpdateUserRequest.class))).thenReturn(updated);

        mockMvc.perform(put("/users/42")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(42))
                .andExpect(jsonPath("$.phone").value("+36309998877"));
    }

    @Test
    void update_shouldReturn400_whenInvalidRequest() throws Exception {
        UpdateUserRequest invalid = UpdateUserRequest.builder().build();

        mockMvc.perform(put("/users/42")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }


    @Test
    void delete_shouldReturn204() throws Exception {
        doNothing().when(userService).delete(42L);

        mockMvc.perform(delete("/users/42"))
                .andExpect(status().isNoContent());
    }


    @Test
    void getOwnProfile_shouldReturn200_whenAuthenticated() throws Exception {
        when(userService.getOwnProfile("bence.kovacs@example.com"))
                .thenReturn(sampleUser());

        var auth =
                new UsernamePasswordAuthenticationToken("bence.kovacs@example.com", null);

        mockMvc.perform(get("/users/me").principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("bence.kovacs@example.com"))
                .andExpect(jsonPath("$.firstName").value("Bence"));
    }
}
