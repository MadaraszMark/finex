package hu.finex.main.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;
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

import hu.finex.main.dto.AccountResponse;
import hu.finex.main.dto.UpdateAccountStatusRequest;
import hu.finex.main.model.enums.AccountStatus;
import hu.finex.main.service.AccountService;

@ActiveProfiles("test")
@WebMvcTest(
    controllers = AdminAccountController.class,
    excludeFilters = {
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = hu.finex.main.config.SecurityConfig.class),
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = hu.finex.main.security.JwtAuthenticationFilter.class)
    }
)
@AutoConfigureMockMvc(addFilters = false)
class AdminAccountControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockBean AccountService accountService;

    @Test
    void listAll_shouldFilterByStatus() throws Exception {
        AccountResponse frozen = AccountResponse.builder()
                .id(3L)
                .balance(new BigDecimal("3052500.00"))
                .status(AccountStatus.FROZEN)
                .build();

        when(accountService.listAll(eq(AccountStatus.FROZEN), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(frozen), PageRequest.of(0, 20), 1));

        mockMvc.perform(get("/admin/accounts").param("status", "FROZEN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].status").value("FROZEN"));
    }

    @Test
    void listAll_shouldReturn400_whenStatusIsUnknown() throws Exception {
        mockMvc.perform(get("/admin/accounts").param("status", "DELETED"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Érvénytelen paraméter: status"));
    }

    @Test
    void updateStatus_shouldReturn200() throws Exception {
        UpdateAccountStatusRequest req = UpdateAccountStatusRequest.builder().status(AccountStatus.FROZEN).build();

        when(accountService.updateStatus(eq(3L), any(UpdateAccountStatusRequest.class))).thenReturn(AccountResponse.builder().id(3L).status(AccountStatus.FROZEN).build());

        mockMvc.perform(patch("/admin/accounts/3/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("FROZEN"));
    }
}
