package hu.finex.main.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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

import hu.finex.main.dto.CardPaymentRequest;
import hu.finex.main.dto.CardResponse;
import hu.finex.main.dto.TransactionResponse;
import hu.finex.main.dto.UpdateCardLimitsRequest;
import hu.finex.main.exception.BusinessException;
import hu.finex.main.model.enums.CardStatus;
import hu.finex.main.model.enums.TransactionType;
import hu.finex.main.service.CardService;

@ActiveProfiles("test")
@WebMvcTest(
    controllers = CardController.class,
    excludeFilters = {
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = hu.finex.main.config.SecurityConfig.class),
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = hu.finex.main.security.JwtAuthenticationFilter.class)
    }
)
@AutoConfigureMockMvc(addFilters = false)
class CardControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockBean CardService cardService;

    private CardResponse sampleCard(CardStatus status) {
        return CardResponse.builder()
                .id(3L)
                .accountId(1L)
                .accountNumber("HU15117730161111101800000001")
                .maskedNumber("**** **** **** 1184")
                .holderName("KOVÁCS ANNA")
                .expiryDate(LocalDate.of(2029, 1, 31))
                .status(status)
                .dailyLimit(new BigDecimal("200000.00"))
                .spentToday(new BigDecimal("12990.00"))
                .onlinePaymentEnabled(true)
                .build();
    }

    @Test
    void listMine_shouldReturn200_withMaskedNumbers() throws Exception {
        when(cardService.listMine()).thenReturn(List.of(sampleCard(CardStatus.ACTIVE)));

        mockMvc.perform(get("/cards"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].maskedNumber").value("**** **** **** 1184"))
                .andExpect(jsonPath("$[0].cardNumber").doesNotExist())
                .andExpect(jsonPath("$[0].expiryDate").value("2029-01-31"));
    }

    @Test
    void block_shouldReturn200() throws Exception {
        when(cardService.block(3L)).thenReturn(sampleCard(CardStatus.BLOCKED));

        mockMvc.perform(patch("/cards/3/block"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("BLOCKED"));
    }

    @Test
    void unblock_shouldReturn400_whenAccountIsNotActive() throws Exception {
        when(cardService.unblock(3L)).thenThrow(new BusinessException("A kártya nem oldható fel, mert a hozzá tartozó számla nem aktív."));

        mockMvc.perform(patch("/cards/3/unblock"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateLimits_shouldReturn200() throws Exception {
        UpdateCardLimitsRequest req = UpdateCardLimitsRequest.builder()
                .dailyLimit(new BigDecimal("50000.00"))
                .onlinePaymentEnabled(false)
                .build();

        when(cardService.updateLimits(eq(3L), any(UpdateCardLimitsRequest.class))).thenReturn(sampleCard(CardStatus.ACTIVE));

        mockMvc.perform(put("/cards/3/limits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    void updateLimits_shouldReturn400_whenLimitIsTooHigh() throws Exception {
        UpdateCardLimitsRequest req = UpdateCardLimitsRequest.builder()
                .dailyLimit(new BigDecimal("5000001"))
                .onlinePaymentEnabled(true)
                .build();

        mockMvc.perform(put("/cards/3/limits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.violations[0].field").value("dailyLimit"));

        verifyNoInteractions(cardService);
    }

    @Test
    void pay_shouldReturn201_andTransaction() throws Exception {
        CardPaymentRequest req = CardPaymentRequest.builder()
                .merchantName("Tesco")
                .amount(new BigDecimal("8990.00"))
                .categoryId(1L)
                .online(false)
                .build();

        TransactionResponse resp = TransactionResponse.builder()
                .id(50L)
                .cardId(3L)
                .type(TransactionType.OUTCOME)
                .amount(new BigDecimal("8990.00"))
                .partnerName("Tesco")
                .build();

        when(cardService.pay(eq(3L), any(CardPaymentRequest.class))).thenReturn(resp);

        mockMvc.perform(post("/cards/3/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(50))
                .andExpect(jsonPath("$.cardId").value(3));
    }

    @Test
    void pay_shouldReturn400_whenDailyLimitExceeded() throws Exception {
        CardPaymentRequest req = CardPaymentRequest.builder()
                .merchantName("MediaMarkt")
                .amount(new BigDecimal("250000.00"))
                .build();

        when(cardService.pay(eq(3L), any(CardPaymentRequest.class))).thenThrow(new BusinessException("A fizetés túllépné a kártya napi limitjét."));

        mockMvc.perform(post("/cards/3/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("A fizetés túllépné a kártya napi limitjét."));
    }
}
