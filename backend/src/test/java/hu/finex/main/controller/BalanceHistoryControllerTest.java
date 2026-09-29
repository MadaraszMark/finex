package hu.finex.main.controller;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import hu.finex.main.dto.BalanceHistoryListItemResponse;
import hu.finex.main.exception.BusinessException;
import hu.finex.main.exception.NotFoundException;
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
class BalanceHistoryControllerTest {

    @Autowired MockMvc mockMvc;

    @MockBean BalanceHistoryService balanceHistoryService;

    @Test
    void listByAccount_shouldReturn200_andList() throws Exception {
        List<BalanceHistoryListItemResponse> items = List.of(
                BalanceHistoryListItemResponse.builder().balance(new BigDecimal("100.00")).createdAt(Instant.parse("2025-03-01T10:00:00Z")).build(),
                BalanceHistoryListItemResponse.builder().balance(new BigDecimal("200.00")).createdAt(Instant.parse("2025-03-02T10:00:00Z")).build()
        );

        when(balanceHistoryService.listByAccount(5L, LocalDate.of(2025, 3, 1), LocalDate.of(2025, 3, 31))).thenReturn(items);

        mockMvc.perform(get("/balance-history/account/5").param("from", "2025-03-01").param("to", "2025-03-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].balance").value(100.00))
                .andExpect(jsonPath("$[1].balance").value(200.00));
    }

    @Test
    void listByAccount_shouldPassNulls_whenNoPeriodGiven() throws Exception {
        when(balanceHistoryService.listByAccount(eq(5L), isNull(), isNull())).thenReturn(List.of());

        mockMvc.perform(get("/balance-history/account/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void listByAccount_shouldReturn400_whenStartAfterEnd() throws Exception {
        when(balanceHistoryService.listByAccount(5L, LocalDate.of(2025, 4, 1), LocalDate.of(2025, 3, 1)))
                .thenThrow(new BusinessException("A kezdő dátum nem lehet későbbi a záró dátumnál."));

        mockMvc.perform(get("/balance-history/account/5").param("from", "2025-04-01").param("to", "2025-03-01"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void listByAccount_shouldReturn400_whenDateIsInvalid() throws Exception {
        mockMvc.perform(get("/balance-history/account/5").param("from", "2025-13-45"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Érvénytelen paraméter: from"));
    }

    @Test
    void listByAccount_shouldReturn404_whenAccountIsNotOwn() throws Exception {
        when(balanceHistoryService.listByAccount(eq(9L), isNull(), isNull())).thenThrow(new NotFoundException("Számla nem található."));

        mockMvc.perform(get("/balance-history/account/9"))
                .andExpect(status().isNotFound());
    }
}
