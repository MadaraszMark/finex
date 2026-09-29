package hu.finex.main.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import hu.finex.main.dto.AccountListItemResponse;
import hu.finex.main.dto.UpdateUserRoleRequest;
import hu.finex.main.dto.UpdateUserStatusRequest;
import hu.finex.main.dto.UserDetailsResponse;
import hu.finex.main.dto.UserListItemResponse;
import hu.finex.main.dto.UserResponse;
import hu.finex.main.exception.BusinessException;
import hu.finex.main.model.enums.UserRole;
import hu.finex.main.model.enums.UserStatus;
import hu.finex.main.service.UserService;

@ActiveProfiles("test")
@WebMvcTest(
    controllers = AdminUserController.class,
    excludeFilters = {
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = hu.finex.main.config.SecurityConfig.class),
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = hu.finex.main.security.JwtAuthenticationFilter.class)
    }
)
@AutoConfigureMockMvc(addFilters = false)
class AdminUserControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockBean UserService userService;

    @Test
    void search_shouldReturn200_andPage() throws Exception {
        UserListItemResponse item = UserListItemResponse.builder()
                .id(2L)
                .fullName("Kovács Anna")
                .email("anna@finex.hu")
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .build();

        when(userService.search(eq("anna"), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(item), PageRequest.of(0, 20), 1));

        mockMvc.perform(get("/admin/users").param("search", "anna"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].fullName").value("Kovács Anna"));
    }

    @Test
    void search_shouldListEveryone_withoutSearchParam() throws Exception {
        when(userService.search(isNull(), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 20), 0));

        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(0));
    }

    @Test
    void getDetails_shouldReturn200() throws Exception {
        UserDetailsResponse resp = UserDetailsResponse.builder()
                .user(UserResponse.builder().id(2L).email("anna@finex.hu").build())
                .accounts(List.of(AccountListItemResponse.builder().id(1L).build()))
                .cards(List.of())
                .savingsAccounts(List.of())
                .build();

        when(userService.getDetails(2L)).thenReturn(resp);

        mockMvc.perform(get("/admin/users/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.email").value("anna@finex.hu"))
                .andExpect(jsonPath("$.accounts.length()").value(1));
    }

    @Test
    void updateStatus_shouldReturn200() throws Exception {
        UpdateUserStatusRequest req = UpdateUserStatusRequest.builder().status(UserStatus.BLOCKED).build();

        when(userService.updateStatus(eq(2L), any(UpdateUserStatusRequest.class))).thenReturn(UserResponse.builder().id(2L).status(UserStatus.BLOCKED).build());

        mockMvc.perform(patch("/admin/users/2/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("BLOCKED"));
    }

    @Test
    void updateStatus_shouldReturn400_whenStatusMissing() throws Exception {
        mockMvc.perform(patch("/admin/users/2/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userService);
    }

    @Test
    void updateRole_shouldReturn400_whenChangingOwnRole() throws Exception {
        UpdateUserRoleRequest req = UpdateUserRoleRequest.builder().role(UserRole.USER).build();

        when(userService.updateRole(eq(1L), any(UpdateUserRoleRequest.class))).thenThrow(new BusinessException("A saját szerepkörödet nem módosíthatod."));

        mockMvc.perform(patch("/admin/users/1/role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("A saját szerepkörödet nem módosíthatod."));
    }
}
