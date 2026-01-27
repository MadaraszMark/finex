package hu.finex.main.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

import hu.finex.main.dto.CategoryResponse;
import hu.finex.main.dto.CreateTransactionRequest;
import hu.finex.main.dto.TransactionListItemResponse;
import hu.finex.main.dto.TransactionResponse;
import hu.finex.main.dto.TransferRequest;
import hu.finex.main.dto.TransferResponse;
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
    void create_shouldReturn200_andBody() throws Exception {
        CreateTransactionRequest req = CreateTransactionRequest.builder()
                .accountId(102L)
                .type(TransactionType.INCOME)
                .amount(new BigDecimal("300000.00"))
                .message("Fizetés a munkahelytől")
                .fromAccount(null)
                .toAccount("HU10101000001234567890000000")
                .currency("EUR")
                .categoryIds(List.of(1L, 3L))
                .build();

        TransactionResponse resp = TransactionResponse.builder()
                .id(5012L)
                .accountId(102L)
                .type(TransactionType.INCOME)
                .amount(new BigDecimal("300000.00"))
                .message("Fizetés a munkahelytől")
                .fromAccount(null)
                .toAccount("HU10101000001234567890000000")
                .currency("EUR")
                .createdAt(Instant.parse("2025-02-15T13:25:44Z"))
                .categories(List.of(
                        CategoryResponse.builder().id(1L).name("Salary").icon("💼").build()
                ))
                .build();

        when(transactionService.create(any(CreateTransactionRequest.class))).thenReturn(resp);

        mockMvc.perform(post("/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(5012))
                .andExpect(jsonPath("$.accountId").value(102))
                .andExpect(jsonPath("$.type").value("INCOME"))
                .andExpect(jsonPath("$.amount").value(300000.00))
                .andExpect(jsonPath("$.currency").value("EUR"))
                .andExpect(jsonPath("$.categories.length()").value(1))
                .andExpect(jsonPath("$.categories[0].id").value(1))
                .andExpect(jsonPath("$.categories[0].name").value("Salary"));
    }

    @Test
    void getById_shouldReturn200_andBody() throws Exception {
        TransactionResponse resp = TransactionResponse.builder()
                .id(5012L)
                .accountId(102L)
                .type(TransactionType.OUTCOME)
                .amount(new BigDecimal("7500.00"))
                .message("Kávézó")
                .currency("HUF")
                .createdAt(Instant.parse("2025-02-15T11:00:15Z"))
                .categories(List.of())
                .build();

        when(transactionService.getById(5012L)).thenReturn(resp);

        mockMvc.perform(get("/transactions/5012"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(5012))
                .andExpect(jsonPath("$.type").value("OUTCOME"))
                .andExpect(jsonPath("$.amount").value(7500.00))
                .andExpect(jsonPath("$.message").value("Kávézó"));
    }

    @Test
    void listByAccount_shouldReturn200_andPage() throws Exception {
        TransactionListItemResponse item1 = TransactionListItemResponse.builder()
                .id(5012L)
                .type(TransactionType.OUTCOME)
                .amount(new BigDecimal("7500.00"))
                .message("Kávézó")
                .currency("HUF")
                .createdAt(Instant.parse("2025-02-15T11:00:15Z"))
                .build();

        TransactionListItemResponse item2 = TransactionListItemResponse.builder()
                .id(5013L)
                .type(TransactionType.INCOME)
                .amount(new BigDecimal("300000.00"))
                .message("Fizetés")
                .currency("HUF")
                .createdAt(Instant.parse("2025-02-16T09:00:00Z"))
                .build();

        Page<TransactionListItemResponse> page =
                new PageImpl<>(List.of(item1, item2), PageRequest.of(0, 10), 2);

        when(transactionService.listByAccount(eq(102L), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/transactions/account/102")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].id").value(5012))
                .andExpect(jsonPath("$.content[0].type").value("OUTCOME"))
                .andExpect(jsonPath("$.content[0].amount").value(7500.00))
                .andExpect(jsonPath("$.content[1].id").value(5013));
    }

    @Test
    void transfer_shouldReturn200_andBody() throws Exception {
        TransferRequest req = TransferRequest.builder()
                .fromAccountId(2L)
                .toAccountId(3L)
                .amount(new BigDecimal("15000.00"))
                .currency("HUF")
                .message("Közös vacsi")
                .categoryIds(List.of(2L, 3L))
                .build();

        TransferResponse resp = TransferResponse.builder()
                .fromAccountId(2L)
                .toAccountId(3L)
                .amount(new BigDecimal("15000.00"))
                .currency("HUF")
                .message("Közös vacsi")
                .categories(List.of(
                        CategoryResponse.builder().id(2L).name("Food").icon("🍔").build(),
                        CategoryResponse.builder().id(3L).name("Entertainment").icon("🎉").build()
                ))
                .fromAccountNewBalance(new BigDecimal("85000.00"))
                .toAccountNewBalance(new BigDecimal("120000.00"))
                .createdAt(Instant.parse("2025-02-15T13:25:44Z"))
                .build();

        when(transactionService.transfer(any(TransferRequest.class))).thenReturn(resp);

        mockMvc.perform(post("/transactions/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.fromAccountId").value(2))
                .andExpect(jsonPath("$.toAccountId").value(3))
                .andExpect(jsonPath("$.amount").value(15000.00))
                .andExpect(jsonPath("$.currency").value("HUF"))
                .andExpect(jsonPath("$.message").value("Közös vacsi"))
                .andExpect(jsonPath("$.fromAccountNewBalance").value(85000.00))
                .andExpect(jsonPath("$.toAccountNewBalance").value(120000.00))
                .andExpect(jsonPath("$.categories.length()").value(2))
                .andExpect(jsonPath("$.categories[0].id").value(2))
                .andExpect(jsonPath("$.categories[0].name").value("Food"));
    }

    @Test
    void transfer_shouldReturn400_whenMissingRequiredFields() throws Exception {
        // fromAccountId/toAccountId/amount/currency hiányzik -> @Valid -> 400
        TransferRequest invalid = TransferRequest.builder().build();

        mockMvc.perform(post("/transactions/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }
}
