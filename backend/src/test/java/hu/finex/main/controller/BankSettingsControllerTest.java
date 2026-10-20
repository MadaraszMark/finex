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

import hu.finex.main.dto.BankSettingsResponse;
import hu.finex.main.service.BankSettingsService;

@ActiveProfiles("test")
@WebMvcTest(
    controllers = BankSettingsController.class,
    excludeFilters = {
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = hu.finex.main.config.SecurityConfig.class),
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = hu.finex.main.security.JwtAuthenticationFilter.class)
    }
)
@AutoConfigureMockMvc(addFilters = false)
class BankSettingsControllerTest {

    @Autowired MockMvc mockMvc;

    @MockBean BankSettingsService bankSettingsService;

    @Test
    void getSettings_shouldReturn200_andBody() throws Exception {
        BankSettingsResponse resp = BankSettingsResponse.builder()
                .transferDailyLimit(new BigDecimal("5000000"))
                .maxAccountsPerUser(5)
                .savingsInterestRate(new BigDecimal("3.50"))
                .cardDefaultDailyLimit(new BigDecimal("200000"))
                .cardDefaultDailyLimitForeign(new BigDecimal("1000"))
                .build();

        when(bankSettingsService.getSettings()).thenReturn(resp);

        mockMvc.perform(get("/settings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transferDailyLimit").value(5000000))
                .andExpect(jsonPath("$.maxAccountsPerUser").value(5))
                .andExpect(jsonPath("$.savingsInterestRate").value(3.5))
                .andExpect(jsonPath("$.cardDefaultDailyLimit").value(200000))
                .andExpect(jsonPath("$.cardDefaultDailyLimitForeign").value(1000));
    }
}
