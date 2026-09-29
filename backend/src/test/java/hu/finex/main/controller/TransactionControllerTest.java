package hu.finex.main.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import hu.finex.main.dto.CategoryResponse;
import hu.finex.main.dto.TransactionListItemResponse;
import hu.finex.main.dto.TransactionResponse;
import hu.finex.main.dto.TransactionSearchRequest;
import hu.finex.main.dto.TransferRequest;
import hu.finex.main.dto.TransferResponse;
import hu.finex.main.exception.BusinessException;
import hu.finex.main.model.enums.TransactionType;
import hu.finex.main.service.TransactionService;

@ActiveProfiles("test")
@WebMvcTest(
    controllers = TransactionController.class,
    excludeFilters = {
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = hu.finex.main.config.SecurityConfig.class),
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = hu.finex.main.security.JwtAuthenticationFilter.class)
    }
)
@AutoConfigureMockMvc(addFilters = false)
class TransactionControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockBean TransactionService transactionService;

    @Test
    void search_shouldReturn200_andBindFiltersAndDefaultPaging() throws Exception {
        TransactionListItemResponse item = TransactionListItemResponse.builder()
                .id(5012L)
                .accountId(102L)
                .type(TransactionType.OUTCOME)
                .amount(new BigDecimal("4990.00"))
                .partnerName("Tesco")
                .currency("HUF")
                .categories(List.of(CategoryResponse.builder().id(1L).name("Élelmiszer").icon("shopping-cart").build()))
                .createdAt(Instant.parse("2025-03-03T17:00:00Z"))
                .build();

        when(transactionService.search(any(TransactionSearchRequest.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(item), PageRequest.of(0, 20), 1));

        mockMvc.perform(get("/transactions")
                        .param("accountId", "102")
                        .param("type", "OUTCOME")
                        .param("from", "2025-03-01")
                        .param("to", "2025-03-31")
                        .param("search", "tesco"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].partnerName").value("Tesco"))
                .andExpect(jsonPath("$.content[0].categories[0].name").value("Élelmiszer"));

        ArgumentCaptor<TransactionSearchRequest> filterCaptor = ArgumentCaptor.forClass(TransactionSearchRequest.class);
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(transactionService).search(filterCaptor.capture(), pageableCaptor.capture());

        assertEquals(102L, filterCaptor.getValue().getAccountId());
        assertEquals(TransactionType.OUTCOME, filterCaptor.getValue().getType());
        assertEquals(LocalDate.of(2025, 3, 1), filterCaptor.getValue().getFrom());
        assertEquals(LocalDate.of(2025, 3, 31), filterCaptor.getValue().getTo());
        assertEquals("tesco", filterCaptor.getValue().getSearch());

        // Alapértelmezés: 20 tétel, a legújabb elöl
        assertEquals(20, pageableCaptor.getValue().getPageSize());
        assertEquals(Sort.Direction.DESC, pageableCaptor.getValue().getSort().getOrderFor("createdAt").getDirection());
    }

    @Test
    void search_shouldReturn400_whenTypeIsUnknown() throws Exception {
        mockMvc.perform(get("/transactions").param("type", "LOTTERY"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(transactionService);
    }

    @Test
    void getById_shouldReturn200_andBody() throws Exception {
        TransactionResponse resp = TransactionResponse.builder()
                .id(5012L)
                .accountId(102L)
                .cardId(7L)
                .type(TransactionType.OUTCOME)
                .amount(new BigDecimal("4990.00"))
                .partnerName("Tesco")
                .currency("HUF")
                .categories(List.of())
                .build();

        when(transactionService.getById(5012L)).thenReturn(resp);

        mockMvc.perform(get("/transactions/5012"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5012))
                .andExpect(jsonPath("$.cardId").value(7))
                .andExpect(jsonPath("$.partnerName").value("Tesco"));
    }

    @Test
    void transfer_shouldReturn200_andBody() throws Exception {
        TransferRequest req = TransferRequest.builder()
                .fromAccountId(102L)
                .toAccountNumber("HU28104000950000521700000003")
                .partnerName("Nagy Bence")
                .amount(new BigDecimal("5000.00"))
                .message("Pizza")
                .categoryIds(List.of(2L))
                .build();

        TransferResponse resp = TransferResponse.builder()
                .transactionId(9001L)
                .fromAccountId(102L)
                .toAccountNumber("HU28104000950000521700000003")
                .partnerName("Nagy Bence")
                .amount(new BigDecimal("5000.00"))
                .currency("HUF")
                .fromAccountNewBalance(new BigDecimal("95000.00"))
                .internal(true)
                .build();

        when(transactionService.transfer(any(TransferRequest.class))).thenReturn(resp);

        mockMvc.perform(post("/transactions/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionId").value(9001))
                .andExpect(jsonPath("$.internal").value(true))
                .andExpect(jsonPath("$.fromAccountNewBalance").value(95000.00));
    }

    @Test
    void transfer_shouldReturn400_whenMissingRequiredFields() throws Exception {
        TransferRequest req = TransferRequest.builder().build();

        mockMvc.perform(post("/transactions/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.violations.length()").value(4));

        verifyNoInteractions(transactionService);
    }

    @Test
    void transfer_shouldReturn400_withMessage_whenBalanceIsInsufficient() throws Exception {
        TransferRequest req = TransferRequest.builder()
                .fromAccountId(102L)
                .toAccountNumber("HU28104000950000521700000003")
                .partnerName("Nagy Bence")
                .amount(new BigDecimal("999999.00"))
                .build();

        when(transactionService.transfer(any(TransferRequest.class))).thenThrow(new BusinessException("Nincs elegendő fedezet a számlán."));

        mockMvc.perform(post("/transactions/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Nincs elegendő fedezet a számlán."));
    }
}
