package hu.finex.main.controller;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;
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

import hu.finex.main.dto.CategorySpendingResponse;
import hu.finex.main.dto.DashboardSummaryResponse;
import hu.finex.main.dto.MonthlySummaryResponse;
import hu.finex.main.service.StatisticsService;

@ActiveProfiles("test")
@WebMvcTest(
    controllers = StatisticsController.class,
    excludeFilters = {
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = hu.finex.main.config.SecurityConfig.class),
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = hu.finex.main.security.JwtAuthenticationFilter.class)
    }
)
@AutoConfigureMockMvc(addFilters = false)
class StatisticsControllerTest {

    @Autowired MockMvc mockMvc;

    @MockBean StatisticsService statisticsService;

    @Test
    void getSummary_shouldDefaultToHuf() throws Exception {
        DashboardSummaryResponse resp = DashboardSummaryResponse.builder()
                .currency("HUF")
                .totalBalance(new BigDecimal("1906620.00"))
                .monthIncome(new BigDecimal("685000.00"))
                .accountCount(2)
                .build();

        when(statisticsService.getSummary("HUF")).thenReturn(resp);

        mockMvc.perform(get("/statistics/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalBalance").value(1906620.00))
                .andExpect(jsonPath("$.accountCount").value(2));
    }

    @Test
    void getMonthlySummary_shouldPassParameters() throws Exception {
        when(statisticsService.getMonthlySummary(12, "EUR", 2L)).thenReturn(List.of(
                MonthlySummaryResponse.builder().month("2025-03").income(new BigDecimal("100.00")).outcome(new BigDecimal("39.99")).transactionCount(2).build()
        ));

        mockMvc.perform(get("/statistics/monthly").param("months", "12").param("currency", "EUR").param("accountId", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].month").value("2025-03"))
                .andExpect(jsonPath("$[0].outcome").value(39.99));
    }

    @Test
    void getMonthlySummary_shouldUseDefaults() throws Exception {
        when(statisticsService.getMonthlySummary(eq(6), eq("HUF"), isNull())).thenReturn(List.of());

        mockMvc.perform(get("/statistics/monthly"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getCategorySpending_shouldReturn200() throws Exception {
        when(statisticsService.getCategorySpending(LocalDate.of(2025, 3, 1), LocalDate.of(2025, 3, 31), "HUF")).thenReturn(List.of(
                new CategorySpendingResponse(1L, "Élelmiszer", "shopping-cart", new BigDecimal("52000.00"), 9L)
        ));

        mockMvc.perform(get("/statistics/categories").param("from", "2025-03-01").param("to", "2025-03-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].categoryName").value("Élelmiszer"))
                .andExpect(jsonPath("$[0].transactionCount").value(9));
    }
}
