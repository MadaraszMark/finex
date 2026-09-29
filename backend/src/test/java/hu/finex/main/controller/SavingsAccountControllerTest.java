package hu.finex.main.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import hu.finex.main.dto.CreateSavingsAccountRequest;
import hu.finex.main.dto.SavingsAccountResponse;
import hu.finex.main.dto.SavingsTransactionResponse;
import hu.finex.main.dto.SavingsTransferRequest;
import hu.finex.main.dto.SavingsTransferResponse;
import hu.finex.main.dto.UpdateSavingsAccountRequest;
import hu.finex.main.model.enums.SavingsStatus;
import hu.finex.main.model.enums.SavingsTransactionType;
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
                .name("Nyaralás")
                .balance(new BigDecimal("250000.00"))
                .currency("HUF")
                .interestRate(new BigDecimal("3.50"))
                .targetAmount(new BigDecimal("600000.00"))
                .progressPercent(new BigDecimal("41.67"))
                .status(SavingsStatus.ACTIVE)
                .build();
    }

    private SavingsTransferResponse sampleTransfer() {
        return SavingsTransferResponse.builder()
                .savingsAccountId(5L)
                .accountId(1L)
                .savingsNewBalance(new BigDecimal("270000.00"))
                .accountNewBalance(new BigDecimal("80000.00"))
                .message("Havi félretétel")
                .createdAt(Instant.parse("2025-03-10T10:00:00Z"))
                .build();
    }

    @Test
    void listMine_shouldReturn200_andList() throws Exception {
        when(savingsAccountService.listMine()).thenReturn(List.of(sampleResponse()));

        mockMvc.perform(get("/savings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Nyaralás"))
                .andExpect(jsonPath("$[0].progressPercent").value(41.67));
    }

    @Test
    void create_shouldReturn201() throws Exception {
        CreateSavingsAccountRequest req = CreateSavingsAccountRequest.builder()
                .name("Nyaralás")
                .accountId(1L)
                .initialDeposit(new BigDecimal("30000.00"))
                .targetAmount(new BigDecimal("600000.00"))
                .build();

        when(savingsAccountService.create(any(CreateSavingsAccountRequest.class))).thenReturn(sampleResponse());

        mockMvc.perform(post("/savings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(5));
    }

    @Test
    void create_shouldReturn400_whenInitialDepositIsNegative() throws Exception {
        CreateSavingsAccountRequest req = CreateSavingsAccountRequest.builder()
                .name("Nyaralás")
                .accountId(1L)
                .initialDeposit(new BigDecimal("-1"))
                .build();

        mockMvc.perform(post("/savings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.violations[0].field").value("initialDeposit"));

        verifyNoInteractions(savingsAccountService);
    }

    @Test
    void getById_shouldReturn200() throws Exception {
        when(savingsAccountService.getById(5L)).thenReturn(sampleResponse());

        mockMvc.perform(get("/savings/5")).andExpect(status().isOk()).andExpect(jsonPath("$.id").value(5));
    }

    @Test
    void update_shouldReturn200() throws Exception {
        UpdateSavingsAccountRequest req = UpdateSavingsAccountRequest.builder()
                .name("Nyaralás 2026")
                .targetAmount(new BigDecimal("800000.00"))
                .build();

        when(savingsAccountService.update(eq(5L), any(UpdateSavingsAccountRequest.class))).thenReturn(sampleResponse());

        mockMvc.perform(put("/savings/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    void depositFromAccount_shouldReturn200() throws Exception {
        SavingsTransferRequest req = SavingsTransferRequest.builder()
                .accountId(1L)
                .amount(new BigDecimal("20000.00"))
                .message("Havi félretétel")
                .build();

        when(savingsAccountService.depositFromAccount(eq(5L), any(SavingsTransferRequest.class))).thenReturn(sampleTransfer());

        mockMvc.perform(post("/savings/5/deposit-from-account")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.savingsNewBalance").value(270000.00));
    }

    @Test
    void withdrawToAccount_shouldReturn200() throws Exception {
        SavingsTransferRequest req = SavingsTransferRequest.builder()
                .accountId(1L)
                .amount(new BigDecimal("15000.00"))
                .build();

        when(savingsAccountService.withdrawToAccount(eq(5L), any(SavingsTransferRequest.class))).thenReturn(sampleTransfer());

        mockMvc.perform(post("/savings/5/withdraw-to-account")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountId").value(1));
    }

    @Test
    void listTransactions_shouldReturn200_andPage() throws Exception {
        SavingsTransactionResponse item = SavingsTransactionResponse.builder()
                .id(9L)
                .type(SavingsTransactionType.INTEREST)
                .amount(new BigDecimal("729.17"))
                .balanceAfter(new BigDecimal("250729.17"))
                .build();

        when(savingsAccountService.listTransactions(eq(5L), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(item), PageRequest.of(0, 20), 1));

        mockMvc.perform(get("/savings/5/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].type").value("INTEREST"));
    }

    @Test
    void close_shouldReturn204_andPayOutToGivenAccount() throws Exception {
        mockMvc.perform(delete("/savings/5").param("accountId", "1"))
                .andExpect(status().isNoContent());

        verify(savingsAccountService).close(5L, 1L);
    }

    @Test
    void close_shouldReturn400_whenAccountIdIsMissing() throws Exception {
        mockMvc.perform(delete("/savings/5"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(savingsAccountService);
    }
}
