package hu.finex.main.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import hu.finex.main.dto.BalanceHistoryListItemResponse;
import hu.finex.main.dto.BalanceHistoryResponse;
import hu.finex.main.service.BalanceHistoryService;

@ActiveProfiles("test")
@WebMvcTest(
    controllers = BalanceHistoryController.class,
    excludeFilters = {
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = hu.finex.main.config.SecurityConfig.class),
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = hu.finex.main.security.JwtAuthenticationFilter.class)
    }
)
@AutoConfigureMockMvc(addFilters = false)
@Import(BalanceHistoryControllerTest.TestExceptionHandler.class)
class BalanceHistoryControllerTest {

    @Autowired MockMvc mockMvc;

    @MockBean BalanceHistoryService balanceHistoryService;

    @RestControllerAdvice
    static class TestExceptionHandler {
        @ExceptionHandler(IllegalArgumentException.class)
        ResponseEntity<String> handleIllegalArgument(IllegalArgumentException ex) {
            return ResponseEntity.badRequest().contentType(MediaType.TEXT_PLAIN).body(ex.getMessage());
        }
    }

    @Test
    void getById_shouldReturn200() throws Exception {
        BalanceHistoryResponse resp = BalanceHistoryResponse.builder().build();
        when(balanceHistoryService.getById(10L)).thenReturn(resp);

        mockMvc.perform(get("/balance-history/10")).andExpect(status().isOk()).andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    void listByAccount_shouldReturn200_andPage() throws Exception {
        List<BalanceHistoryListItemResponse> items = List.of(
                BalanceHistoryListItemResponse.builder()
                        .balance(new BigDecimal("100.00"))
                        .createdAt(Instant.parse("2025-02-12T10:15:30Z"))
                        .build(),
                BalanceHistoryListItemResponse.builder()
                        .balance(new BigDecimal("200.00"))
                        .createdAt(Instant.parse("2025-02-13T10:15:30Z"))
                        .build()
        );

        Pageable pageable = PageRequest.of(0, 20);
        Page<BalanceHistoryListItemResponse> page = new PageImpl<>(items, pageable, items.size());

        when(balanceHistoryService.listByAccount(eq(5L), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/balance-history/account/5")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].balance").value(100.00))
                .andExpect(jsonPath("$.content[1].balance").value(200.00));
    }

    @Test
    void listByAccountBetween_shouldReturn200() throws Exception {
        OffsetDateTime start = OffsetDateTime.parse("2026-01-01T00:00:00+01:00");
        OffsetDateTime end   = OffsetDateTime.parse("2026-01-10T00:00:00+01:00");

        List<BalanceHistoryListItemResponse> items = List.of(
                BalanceHistoryListItemResponse.builder().balance(new BigDecimal("123.45")).createdAt(Instant.parse("2025-02-12T10:15:30Z")).build()
        );

        Pageable pageable = PageRequest.of(0, 10);
        Page<BalanceHistoryListItemResponse> page = new PageImpl<>(items, pageable, 1);

        when(balanceHistoryService.listByAccountBetween(eq(5L), eq(start), eq(end), any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/balance-history/account/5/between")
                        .param("start", start.toString())
                        .param("end", end.toString())
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].balance").value(123.45));
    }

    @Test
    void listByAccountBetween_shouldReturn500_whenStartAfterEnd() throws Exception {
        OffsetDateTime start = OffsetDateTime.parse("2026-01-10T00:00:00+01:00");
        OffsetDateTime end   = OffsetDateTime.parse("2026-01-01T00:00:00+01:00");

        mockMvc.perform(get("/balance-history/account/5/between")
                        .param("start", start.toString())
                        .param("end", end.toString())
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("A kezdő időpont nem lehet később")));
    }

}
