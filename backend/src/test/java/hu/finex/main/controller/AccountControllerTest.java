package hu.finex.main.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
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

import hu.finex.main.dto.AccountResponse;
import hu.finex.main.dto.CreateAccountRequest;
import hu.finex.main.dto.DepositRequest;
import hu.finex.main.dto.StatementResponse;
import hu.finex.main.exception.BusinessException;
import hu.finex.main.exception.NotFoundException;
import hu.finex.main.model.enums.AccountStatus;
import hu.finex.main.model.enums.AccountType;
import hu.finex.main.service.AccountService;

@ActiveProfiles("test")
@WebMvcTest(
    controllers = AccountController.class,
    excludeFilters = {
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = hu.finex.main.config.SecurityConfig.class),
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = hu.finex.main.security.JwtAuthenticationFilter.class)
    }
)
@AutoConfigureMockMvc(addFilters = false)
class AccountControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockBean AccountService accountService;

    private AccountResponse sampleResponse() {
        return AccountResponse.builder()
                .id(10L)
                .userId(1L)
                .name("Fő számla")
                .accountNumber("HU15117730161111101800000001")
                .balance(new BigDecimal("1500.00"))
                .currency("HUF")
                .accountType(AccountType.CURRENT)
                .status(AccountStatus.ACTIVE)
                .build();
    }

    @Test
    void listMine_shouldReturn200_andList() throws Exception {
        when(accountService.listMine()).thenReturn(List.of(sampleResponse()));

        mockMvc.perform(get("/accounts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Fő számla"));
    }

    @Test
    void open_shouldReturn201_andBody() throws Exception {
        CreateAccountRequest req = CreateAccountRequest.builder()
                .name("Fő számla")
                .currency("HUF")
                .build();

        when(accountService.open(any(CreateAccountRequest.class))).thenReturn(sampleResponse());

        mockMvc.perform(post("/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.accountNumber").value("HU15117730161111101800000001"));
    }

    @Test
    void open_shouldReturn400_whenCurrencyIsNotSupported() throws Exception {
        CreateAccountRequest req = CreateAccountRequest.builder()
                .name("Font számla")
                .currency("GBP")
                .build();

        mockMvc.perform(post("/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.violations[0].field").value("currency"));

        verifyNoInteractions(accountService);
    }

    @Test
    void getById_shouldReturn200() throws Exception {
        when(accountService.getById(10L)).thenReturn(sampleResponse());

        mockMvc.perform(get("/accounts/10")).andExpect(status().isOk()).andExpect(jsonPath("$.id").value(10));
    }

    @Test
    void getById_shouldReturn404_whenNotFound() throws Exception {
        when(accountService.getById(10L)).thenThrow(new NotFoundException("Számla nem található."));

        mockMvc.perform(get("/accounts/10"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Számla nem található."));
    }

    @Test
    void getMyAccount_shouldReturn200() throws Exception {
        when(accountService.getMyAccount()).thenReturn(sampleResponse());

        mockMvc.perform(get("/accounts/me")).andExpect(status().isOk()).andExpect(jsonPath("$.id").value(10));
    }

    @Test
    void deposit_shouldReturn200() throws Exception {
        DepositRequest req = DepositRequest.builder()
                .amount(new BigDecimal("250.00"))
                .message("Készpénz")
                .build();

        when(accountService.deposit(eq(10L), any(DepositRequest.class))).thenReturn(sampleResponse());

        mockMvc.perform(post("/accounts/10/deposit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(1500.00));
    }

    @Test
    void deposit_shouldReturn400_whenAmountIsNegative() throws Exception {
        DepositRequest req = DepositRequest.builder()
                .amount(new BigDecimal("-5"))
                .build();

        mockMvc.perform(post("/accounts/10/deposit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(accountService);
    }

    @Test
    void getStatement_shouldReturn200_andPassDates() throws Exception {
        StatementResponse resp = StatementResponse.builder()
                .accountId(10L)
                .openingBalance(new BigDecimal("100.00"))
                .closingBalance(new BigDecimal("150.00"))
                .items(List.of())
                .build();

        when(accountService.getStatement(10L, LocalDate.of(2025, 3, 1), LocalDate.of(2025, 3, 31))).thenReturn(resp);

        mockMvc.perform(get("/accounts/10/statement").param("from", "2025-03-01").param("to", "2025-03-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.openingBalance").value(100.00))
                .andExpect(jsonPath("$.closingBalance").value(150.00));
    }

    @Test
    void getStatement_shouldReturn400_whenDateIsMissing() throws Exception {
        mockMvc.perform(get("/accounts/10/statement").param("from", "2025-03-01"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Hiányzó paraméter: to"));
    }

    @Test
    void close_shouldReturn204() throws Exception {
        mockMvc.perform(delete("/accounts/10")).andExpect(status().isNoContent());

        verify(accountService).close(10L);
    }

    @Test
    void close_shouldReturn400_whenBalanceIsNotZero() throws Exception {
        doThrow(new BusinessException("Csak nulla egyenlegű számla zárható le.")).when(accountService).close(10L);

        mockMvc.perform(delete("/accounts/10"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Csak nulla egyenlegű számla zárható le."));
    }
}
