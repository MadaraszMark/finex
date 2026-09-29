package hu.finex.main.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import hu.finex.main.dto.CreateStandingOrderRequest;
import hu.finex.main.dto.StandingOrderResponse;
import hu.finex.main.dto.UpdateStandingOrderRequest;
import hu.finex.main.model.enums.StandingOrderFrequency;
import hu.finex.main.service.StandingOrderService;

@ActiveProfiles("test")
@WebMvcTest(
    controllers = StandingOrderController.class,
    excludeFilters = {
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = hu.finex.main.config.SecurityConfig.class),
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = hu.finex.main.security.JwtAuthenticationFilter.class)
    }
)
@AutoConfigureMockMvc(addFilters = false)
class StandingOrderControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockBean StandingOrderService standingOrderService;

    private StandingOrderResponse sample() {
        return StandingOrderResponse.builder()
                .id(3L)
                .accountId(1L)
                .toAccountNumber("HU66109180010000048900240017")
                .partnerName("Tóth Gábor")
                .amount(new BigDecimal("220000.00"))
                .currency("HUF")
                .frequency(StandingOrderFrequency.MONTHLY)
                .nextExecutionDate(LocalDate.of(2030, 1, 6))
                .active(true)
                .build();
    }

    @Test
    void listMine_shouldReturn200() throws Exception {
        when(standingOrderService.listMine()).thenReturn(List.of(sample()));

        mockMvc.perform(get("/standing-orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].frequency").value("MONTHLY"))
                .andExpect(jsonPath("$[0].nextExecutionDate").value("2030-01-06"));
    }

    @Test
    void create_shouldReturn201() throws Exception {
        CreateStandingOrderRequest req = CreateStandingOrderRequest.builder()
                .accountId(1L)
                .toAccountNumber("HU66109180010000048900240017")
                .partnerName("Tóth Gábor")
                .amount(new BigDecimal("220000.00"))
                .message("Albérlet")
                .frequency(StandingOrderFrequency.MONTHLY)
                .firstExecutionDate(LocalDate.now().plusDays(3))
                .build();

        when(standingOrderService.create(any(CreateStandingOrderRequest.class))).thenReturn(sample());

        mockMvc.perform(post("/standing-orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(3));
    }

    @Test
    void create_shouldReturn400_whenFirstExecutionIsInThePast() throws Exception {
        CreateStandingOrderRequest req = CreateStandingOrderRequest.builder()
                .accountId(1L)
                .toAccountNumber("HU66109180010000048900240017")
                .partnerName("Tóth Gábor")
                .amount(new BigDecimal("220000.00"))
                .frequency(StandingOrderFrequency.MONTHLY)
                .firstExecutionDate(LocalDate.now().minusDays(1))
                .build();

        mockMvc.perform(post("/standing-orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.violations[0].field").value("firstExecutionDate"));

        verifyNoInteractions(standingOrderService);
    }

    @Test
    void create_shouldReturn400_whenFrequencyIsUnknown() throws Exception {
        String json = "{\"accountId\":1,\"toAccountNumber\":\"HU66109180010000048900240017\",\"partnerName\":\"Tóth Gábor\","
                + "\"amount\":1000,\"frequency\":\"DAILY\",\"firstExecutionDate\":\"2030-01-06\"}";

        mockMvc.perform(post("/standing-orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Hibás formátumú kérés."));
    }

    @Test
    void update_shouldReturn200() throws Exception {
        UpdateStandingOrderRequest req = UpdateStandingOrderRequest.builder()
                .amount(new BigDecimal("235000.00"))
                .frequency(StandingOrderFrequency.MONTHLY)
                .nextExecutionDate(LocalDate.now().plusDays(10))
                .build();

        when(standingOrderService.update(eq(3L), any(UpdateStandingOrderRequest.class))).thenReturn(sample());

        mockMvc.perform(put("/standing-orders/3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    void cancel_shouldReturn204() throws Exception {
        mockMvc.perform(delete("/standing-orders/3")).andExpect(status().isNoContent());

        verify(standingOrderService).cancel(3L);
    }
}
