package hu.finex.main.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import hu.finex.main.dto.AdminDashboardResponse;
import hu.finex.main.dto.JobRunResponse;
import hu.finex.main.scheduler.SavingsInterestScheduler;
import hu.finex.main.scheduler.StandingOrderScheduler;
import hu.finex.main.service.AdminDashboardService;

@ActiveProfiles("test")
@WebMvcTest(
    controllers = AdminDashboardController.class,
    excludeFilters = {
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = hu.finex.main.config.SecurityConfig.class),
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = hu.finex.main.security.JwtAuthenticationFilter.class)
    }
)
@AutoConfigureMockMvc(addFilters = false)
class AdminDashboardControllerTest {

    @Autowired MockMvc mockMvc;

    @MockBean AdminDashboardService adminDashboardService;
    @MockBean SavingsInterestScheduler savingsInterestScheduler;
    @MockBean StandingOrderScheduler standingOrderScheduler;

    @Test
    void getDashboard_shouldReturn200() throws Exception {
        AdminDashboardResponse resp = AdminDashboardResponse.builder()
                .userCount(3)
                .accountCount(4)
                .totalDepositsHuf(new BigDecimal("5059120.00"))
                .openTicketCount(1)
                .failedLoginsLast24h(2)
                .build();

        when(adminDashboardService.getDashboard()).thenReturn(resp);

        mockMvc.perform(get("/admin/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userCount").value(3))
                .andExpect(jsonPath("$.totalDepositsHuf").value(5059120.00))
                .andExpect(jsonPath("$.failedLoginsLast24h").value(2));
    }

    @Test
    void runSavingsInterest_shouldReturnJobResult() throws Exception {
        when(savingsInterestScheduler.run()).thenReturn(JobRunResponse.builder().job("Havi kamatjóváírás").processed(2).skipped(0).build());

        mockMvc.perform(post("/admin/jobs/savings-interest"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.processed").value(2));
    }

    @Test
    void runStandingOrders_shouldReturnJobResult() throws Exception {
        when(standingOrderScheduler.run()).thenReturn(JobRunResponse.builder().job("Rendszeres átutalások").processed(1).skipped(1).build());

        mockMvc.perform(post("/admin/jobs/standing-orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.skipped").value(1));
    }
}
