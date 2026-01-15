package hu.finex.main.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import hu.finex.main.dto.AccountListItemResponse;
import hu.finex.main.dto.AccountResponse;
import hu.finex.main.dto.CreateAccountRequest;
import hu.finex.main.dto.DepositRequest;
import hu.finex.main.dto.UpdateAccountStatusRequest;
import hu.finex.main.dto.UpdateCardNumberRequest;
import hu.finex.main.model.User;
import hu.finex.main.model.enums.AccountStatus;
import hu.finex.main.model.enums.AccountType;
import hu.finex.main.repository.UserRepository;
import hu.finex.main.service.AccountService;

@WebMvcTest(
		  controllers = AccountController.class,
		  excludeAutoConfiguration = {
		      org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
		      org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration.class,
		      org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientWebSecurityAutoConfiguration.class,
		      org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientAutoConfiguration.class,
		      org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration.class,
		      org.springframework.boot.autoconfigure.security.saml2.Saml2RelyingPartyAutoConfiguration.class
		  }
		)
		@AutoConfigureMockMvc(addFilters = false)
class AccountControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockBean AccountService accountService;
    @MockBean UserRepository userRepository;

    @Test
    void create_shouldReturn201_andBody() throws Exception {
    	CreateAccountRequest req = CreateAccountRequest.builder()
    	        .userId(1L)
    	        .currency("HUF")
    	        .accountType(AccountType.CURRENT)
    	        .build();

    	AccountResponse resp = AccountResponse.builder()
    	        .id(10L)
    	        .userId(1L)
    	        .currency("HUF")
    	        .accountType(AccountType.CURRENT)
    	        .build();


        when(accountService.create(any(CreateAccountRequest.class))).thenReturn(resp);

        mockMvc.perform(post("/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.userId").value(1));
    }

    @Test
    void getById_shouldReturn200() throws Exception {
        AccountResponse resp = AccountResponse.builder()
                .id(10L)
                .userId(1L)
                .currency("HUF")
                .build();

        when(accountService.getById(10L)).thenReturn(resp);

        mockMvc.perform(get("/accounts/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10));
    }

    @Test
    void getMyAccount_shouldReturn200_whenUserFound() throws Exception {
        Principal principal = () -> "me@finex.hu";

        User user = User.builder().id(1L).email("me@finex.hu").build();
        when(userRepository.findByEmailIgnoreCase("me@finex.hu")).thenReturn(java.util.Optional.of(user));

        AccountResponse resp = AccountResponse.builder().id(99L).userId(1L).build();
        when(accountService.getMyAccount(1L)).thenReturn(resp);

        mockMvc.perform(get("/accounts/me").principal(principal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(99));
    }

    @Test
    void getMyAccount_shouldReturn404_whenUserMissing() throws Exception {
        Principal principal = () -> "missing@finex.hu";
        when(userRepository.findByEmailIgnoreCase("missing@finex.hu")).thenReturn(java.util.Optional.empty());

        mockMvc.perform(get("/accounts/me").principal(principal))
                .andExpect(status().isNotFound());
    }

    @Test
    void listByUser_shouldReturn200_andList() throws Exception {
        List<AccountListItemResponse> list = List.of(
                AccountListItemResponse.builder().id(1L).accountNumber("A1").build(),
                AccountListItemResponse.builder().id(2L).accountNumber("A2").build()
        );

        when(accountService.listByUser(5L)).thenReturn(list);

        mockMvc.perform(get("/accounts/user/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void updateCard_shouldReturn200() throws Exception {
        UpdateCardNumberRequest req = UpdateCardNumberRequest.builder()
                .cardNumber("4895121234567890")
                .build();

        AccountResponse resp = AccountResponse.builder().id(10L).cardNumber("4895121234567890").build();
        when(accountService.updateCardNumber(eq(10L), any(UpdateCardNumberRequest.class))).thenReturn(resp);

        mockMvc.perform(put("/accounts/10/card")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.cardNumber").value("4895121234567890"));
    }

    @Test
    void updateStatus_shouldReturn200() throws Exception {
        UpdateAccountStatusRequest req = UpdateAccountStatusRequest.builder()
                .status(AccountStatus.FROZEN)
                .build();

        AccountResponse resp = AccountResponse.builder().id(10L).status(AccountStatus.FROZEN).build();
        when(accountService.updateStatus(eq(10L), any(UpdateAccountStatusRequest.class))).thenReturn(resp);

        mockMvc.perform(put("/accounts/10/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("FROZEN"));
    }

    @Test
    void deposit_shouldReturn200_whenUserFound() throws Exception {
        Principal principal = () -> "me@finex.hu";
        User user = User.builder().id(1L).email("me@finex.hu").build();
        when(userRepository.findByEmailIgnoreCase("me@finex.hu")).thenReturn(java.util.Optional.of(user));

        DepositRequest req = DepositRequest.builder()
                .amount(new BigDecimal("100.00"))
                .message("topup")
                .build();

        AccountResponse resp = AccountResponse.builder().id(10L).balance(new BigDecimal("200.00")).build();
        when(accountService.deposit(eq(10L), eq(new BigDecimal("100.00")), eq("topup"), eq(1L))).thenReturn(resp);

        mockMvc.perform(post("/accounts/10/deposit")
                        .principal(principal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10));
    }

    @Test
    void delete_shouldReturn204() throws Exception {
        mockMvc.perform(delete("/accounts/10"))
                .andExpect(status().isNoContent());
    }
}
