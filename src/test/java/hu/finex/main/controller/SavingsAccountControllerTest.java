package hu.finex.main.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import hu.finex.main.dto.CreateSavingsAccountRequest;
import hu.finex.main.dto.SavingsAccountResponse;
import hu.finex.main.dto.SavingsTransferRequest;
import hu.finex.main.dto.SavingsTransferResponse;
import hu.finex.main.dto.UpdateSavingsAccountRequest;
import hu.finex.main.model.enums.SavingsStatus;
import hu.finex.main.service.SavingsAccountService;

@ActiveProfiles("test")
@WebMvcTest(
    controllers = SavingsAccountController.class,
    excludeFilters = {
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = hu.finex.main.config.SecurityConfig.class),
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = hu.finex.main.security.JwtAuthenticationFilter.class)
    }
)
@AutoConfigureMockMvc(addFilters = false)
class SavingsAccountControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockBean SavingsAccountService savingsAccountService;

    private SavingsAccountResponse sampleResponse() {
        return SavingsAccountResponse.builder()
                .id(5L)
                .userId(12L)
                .name("Havi megtakarítás")
                .balance(new BigDecimal("83000.00"))
                .currency("HUF")
                .interestRate(new BigDecimal("2.5"))
                .status(SavingsStatus.ACTIVE)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    @Test
    void create_shouldReturn200() throws Exception {
        CreateSavingsAccountRequest req = CreateSavingsAccountRequest.builder()
                .userId(12L)
                .name("Havi megtakarítás")
                .initialBalance(new BigDecimal("50000"))
                .currency("HUF")
                .interestRate(new BigDecimal("2.5"))
                .build();

        when(savingsAccountService.create(any())).thenReturn(sampleResponse());

        mockMvc.perform(post("/savings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Havi megtakarítás"));
    }

    @Test
    void getById_shouldReturn200() throws Exception {
        when(savingsAccountService.getById(5L)).thenReturn(sampleResponse());

        mockMvc.perform(get("/savings/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5));
    }

    @Test
    void listByUser_shouldReturn200_andPage() throws Exception {
        Page<SavingsAccountResponse> page =
                new PageImpl<>(List.of(sampleResponse()), PageRequest.of(0, 10), 1);

        when(savingsAccountService.listByUser(eq(12L), any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/savings/user/12")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1));
    }

    @Test
    void listByUserAndStatus_shouldReturn200() throws Exception {
        Page<SavingsAccountResponse> page =
                new PageImpl<>(List.of(sampleResponse()), PageRequest.of(0, 10), 1);

        when(savingsAccountService.listByUserAndStatus(eq(12L), eq(SavingsStatus.ACTIVE), any()))
                .thenReturn(page);

        mockMvc.perform(get("/savings/user/12/status/ACTIVE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].status").value("ACTIVE"));
    }

    @Test
    void listAboveBalance_shouldReturn200() throws Exception {
        Page<SavingsAccountResponse> page =
                new PageImpl<>(List.of(sampleResponse()), PageRequest.of(0, 10), 1);

        when(savingsAccountService.listAboveBalance(eq(12L), eq(new BigDecimal("10000")), any()))
                .thenReturn(page);

        mockMvc.perform(get("/savings/user/12/min-balance/10000"))
                .andExpect(status().isOk());
    }

    @Test
    void update_shouldReturn200() throws Exception {
        UpdateSavingsAccountRequest req = UpdateSavingsAccountRequest.builder()
                .name("Új név")
                .build();

        when(savingsAccountService.update(eq(5L), any()))
                .thenReturn(sampleResponse());

        mockMvc.perform(put("/savings/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    void depositFromAccount_shouldReturn200() throws Exception {
        SavingsTransferRequest req = SavingsTransferRequest.builder()
                .accountId(3L)
                .amount(new BigDecimal("10000"))
                .message("Topup")
                .build();

        when(savingsAccountService.depositFromAccount(eq(5L), any()))
                .thenReturn(SavingsTransferResponse.builder().build());

        mockMvc.perform(post("/savings/5/deposit-from-account")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    void withdrawToAccount_shouldReturn200() throws Exception {
        SavingsTransferRequest req = SavingsTransferRequest.builder()
                .accountId(3L)
                .amount(new BigDecimal("5000"))
                .message("Withdraw")
                .build();

        when(savingsAccountService.withdrawToAccount(eq(5L), any()))
                .thenReturn(SavingsTransferResponse.builder().build());

        mockMvc.perform(post("/savings/5/withdraw-to-account")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    void delete_shouldReturn204() throws Exception {
        doNothing().when(savingsAccountService).delete(5L);

        mockMvc.perform(delete("/savings/5"))
                .andExpect(status().isNoContent());
    }
}
