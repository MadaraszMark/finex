package hu.finex.main.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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
import hu.finex.main.dto.ChangePasswordRequest;
import hu.finex.main.dto.UpdateUserRequest;
import hu.finex.main.dto.UserResponse;
import hu.finex.main.exception.BusinessException;
import hu.finex.main.model.enums.UserRole;
import hu.finex.main.model.enums.UserStatus;
import hu.finex.main.service.RevokedTokenService;
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
    @MockBean RevokedTokenService revokedTokenService;

    private UserResponse sampleUser() {
        return UserResponse.builder()
                .id(42L)
                .firstName("Anna")
                .lastName("Kovács")
                .email("anna@finex.hu")
                .phone("+36301234567")
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .build();
    }

    @Test
    void getOwnProfile_shouldReturn200_andBody() throws Exception {
        when(userService.getOwnProfile()).thenReturn(sampleUser());

        mockMvc.perform(get("/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(42))
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    @Test
    void updateOwnProfile_shouldReturn200_andBody() throws Exception {
        UpdateUserRequest req = UpdateUserRequest.builder()
                .firstName("Anna")
                .lastName("Kovács")
                .email("anna@finex.hu")
                .phone("+36301234567")
                .build();

        when(userService.updateOwnProfile(any(UpdateUserRequest.class))).thenReturn(sampleUser());

        mockMvc.perform(put("/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("anna@finex.hu"));
    }

    @Test
    void updateOwnProfile_shouldReturn400_whenInvalidRequest() throws Exception {
        UpdateUserRequest req = UpdateUserRequest.builder()
                .firstName("")
                .lastName("Kovács")
                .email("nem-email")
                .build();

        mockMvc.perform(put("/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.violations.length()").value(2));

        verifyNoInteractions(userService);
    }

    // Jelszócsere után a többi token érvénytelen, az aktuális munkamenet új tokent kap
    @Test
    void changePassword_shouldReturn200_withNewToken() throws Exception {
        ChangePasswordRequest req = ChangePasswordRequest.builder()
                .currentPassword("Regi12345")
                .newPassword("Uj123456")
                .build();

        when(userService.changePassword(any(ChangePasswordRequest.class))).thenReturn(AuthResponse.builder().token("uj.token").build());

        mockMvc.perform(put("/users/me/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("uj.token"));

        verify(userService).changePassword(any(ChangePasswordRequest.class));
    }

    @Test
    void logout_shouldRevokeTheBearerToken() throws Exception {
        mockMvc.perform(post("/users/me/logout").header("Authorization", "Bearer a.b.c"))
                .andExpect(status().isNoContent());

        verify(revokedTokenService).revoke("a.b.c");
    }

    @Test
    void logoutEverywhere_shouldReturnNewToken() throws Exception {
        when(userService.logoutEverywhere()).thenReturn(AuthResponse.builder().token("friss.token").build());

        mockMvc.perform(post("/users/me/logout-all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("friss.token"));
    }

    @Test
    void changePassword_shouldReturn400_whenNewPasswordIsTooShort() throws Exception {
        ChangePasswordRequest req = ChangePasswordRequest.builder()
                .currentPassword("Regi12345")
                .newPassword("a1")
                .build();

        mockMvc.perform(put("/users/me/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userService);
    }

    @Test
    void changePassword_shouldReturn400_whenCurrentPasswordIsWrong() throws Exception {
        ChangePasswordRequest req = ChangePasswordRequest.builder()
                .currentPassword("Rossz1234")
                .newPassword("Uj123456")
                .build();

        doThrow(new BusinessException("A jelenlegi jelszó hibás.")).when(userService).changePassword(any(ChangePasswordRequest.class));

        mockMvc.perform(put("/users/me/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("A jelenlegi jelszó hibás."));
    }
}
