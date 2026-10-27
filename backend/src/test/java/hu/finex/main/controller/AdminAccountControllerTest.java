package hu.finex.main.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoInteractions;
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
import hu.finex.main.dto.DepositRequest;
import hu.finex.main.dto.TransactionListItemResponse;
import hu.finex.main.dto.UpdateAccountStatusRequest;
import hu.finex.main.model.enums.AccountStatus;
import hu.finex.main.service.AccountService;
import hu.finex.main.service.TransactionService;

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
    @MockBean TransactionService transactionService;

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

    @Test
    void deposit_shouldReturn200() throws Exception {
        DepositRequest req = DepositRequest.builder()
                .amount(new BigDecimal("250.00"))
                .message("Pénztári befizetés")
                .build();

        when(accountService.depositByAdmin(eq(10L), any(DepositRequest.class))).thenReturn(AccountResponse.builder().id(10L).balance(new BigDecimal("1250.00")).build());

        mockMvc.perform(post("/admin/accounts/10/deposit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(1250.00));
    }

    @Test
    void deposit_shouldReturn400_whenAmountHasTooManyDecimals() throws Exception {
        DepositRequest req = DepositRequest.builder()
                .amount(new BigDecimal("10.001"))
                .build();

        mockMvc.perform(post("/admin/accounts/10/deposit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(accountService);
    }

    @Test
    void listTransactions_shouldReturnPage() throws Exception {
        TransactionListItemResponse item = TransactionListItemResponse.builder().id(5L).reversed(true).build();
        when(transactionService.listByAccountForAdmin(eq(10L), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(item), PageRequest.of(0, 20), 1));

        mockMvc.perform(get("/admin/accounts/10/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(5))
                .andExpect(jsonPath("$.content[0].reversed").value(true));
    }
}
