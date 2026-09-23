package hu.finex.main.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import hu.finex.main.dto.AuthResponse;
import hu.finex.main.dto.CreateUserRequest;
import hu.finex.main.dto.LoginRequest;
import hu.finex.main.dto.UserResponse;
import hu.finex.main.service.AuthService;

@ActiveProfiles("test")
@WebMvcTest(
    controllers = AuthController.class,
    excludeFilters = {
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = hu.finex.main.config.SecurityConfig.class),
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = hu.finex.main.security.JwtAuthenticationFilter.class)
    }
)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockBean AuthService authService;

    @Test
    void register_shouldReturn201_andBody() throws Exception {
        CreateUserRequest req = CreateUserRequest.builder()
                .firstName("Bence")
                .lastName("Kovács")
                .email("bence.kovacs@example.com")
                .phone("+36301234567")
                .password("TitkosJelszo123")
                .role("USER")
                .build();

        UserResponse resp = UserResponse.builder()
                .id(1L)
                .email("bence.kovacs@example.com")
                .build();

        when(authService.register(any(CreateUserRequest.class))).thenReturn(resp);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("bence.kovacs@example.com"));
    }

    @Test
    void register_shouldReturn400_whenMissingRequiredFields() throws Exception {
        CreateUserRequest req = CreateUserRequest.builder().build();

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_shouldReturn400_whenEmailInvalid() throws Exception {
        CreateUserRequest req = CreateUserRequest.builder()
                .firstName("Bence")
                .lastName("Kovács")
                .email("not-an-email")
                .password("TitkosJelszo123")
                .role("USER")
                .build();

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_shouldReturn200_andBody() throws Exception {
        LoginRequest req = LoginRequest.builder()
                .email("bence.kovacs@example.com")
                .password("TitkosJelszo123")
                .build();

        AuthResponse resp = AuthResponse.builder()
                .token("jwt-token-here")
                .build();

        when(authService.login(any(LoginRequest.class), eq("1.2.3.4"), eq("JUnit")))
                .thenReturn(resp);

        mockMvc.perform(post("/auth/login")
                        .with(r -> { r.setRemoteAddr("1.2.3.4"); return r; })
                        .header("User-Agent", "JUnit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.token").value("jwt-token-here"));
    }

    @Test
    void login_shouldReturn400_whenInvalidBody() throws Exception {
        LoginRequest req = LoginRequest.builder().build();

        mockMvc.perform(post("/auth/login")
                        .with(r -> { r.setRemoteAddr("1.2.3.4"); return r; })
                        .header("User-Agent", "JUnit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }
}
