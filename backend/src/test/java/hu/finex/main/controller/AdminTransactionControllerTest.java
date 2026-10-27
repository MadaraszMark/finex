package hu.finex.main.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.Instant;
import java.util.List;

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

import hu.finex.main.dto.ReconciliationResponse;
import hu.finex.main.dto.ReverseTransactionRequest;
import hu.finex.main.dto.TransactionResponse;
import hu.finex.main.exception.BusinessException;
import hu.finex.main.service.ReconciliationService;
import hu.finex.main.service.TransactionService;

@ActiveProfiles("test")
@WebMvcTest(
    controllers = AdminTransactionController.class,
    excludeFilters = {
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = hu.finex.main.config.SecurityConfig.class),
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = hu.finex.main.security.JwtAuthenticationFilter.class)
    }
)
@AutoConfigureMockMvc(addFilters = false)
class AdminTransactionControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockBean TransactionService transactionService;
    @MockBean ReconciliationService reconciliationService;

    @Test
    void reverse_shouldReturnTheReversalEntry() throws Exception {
        when(transactionService.reverse(eq(5L), any(ReverseTransactionRequest.class))).thenReturn(TransactionResponse.builder().id(6L).reversalOfId(5L).build());

        mockMvc.perform(post("/admin/transactions/5/reverse")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ReverseTransactionRequest.builder().reason("Kétszer terhelt").build())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reversalOfId").value(5));
    }

    @Test
    void reverse_shouldReturn400_whenReasonIsMissing() throws Exception {
        mockMvc.perform(post("/admin/transactions/5/reverse")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\":\"  \"}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(transactionService);
    }

    @Test
    void reverse_shouldReturn400_whenAlreadyReversed() throws Exception {
        when(transactionService.reverse(eq(5L), any(ReverseTransactionRequest.class))).thenThrow(new BusinessException("Ezt a tételt már sztornózták."));

        mockMvc.perform(post("/admin/transactions/5/reverse")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ReverseTransactionRequest.builder().reason("x").build())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Ezt a tételt már sztornózták."));
    }

    @Test
    void reconcile_shouldReturnTheResult() throws Exception {
        when(reconciliationService.check()).thenReturn(ReconciliationResponse.builder()
                .checkedAt(Instant.now()).accountCount(7).savingsCount(3).balanced(true).mismatches(List.of()).build());

        mockMvc.perform(get("/admin/reconciliation"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balanced").value(true))
                .andExpect(jsonPath("$.accountCount").value(7));
    }
}
