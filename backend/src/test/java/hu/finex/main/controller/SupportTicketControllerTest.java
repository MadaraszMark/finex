package hu.finex.main.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import hu.finex.main.dto.CreateSupportTicketRequest;
import hu.finex.main.dto.CreateTicketMessageRequest;
import hu.finex.main.dto.SupportTicketListItemResponse;
import hu.finex.main.dto.SupportTicketResponse;
import hu.finex.main.dto.TicketMessageResponse;
import hu.finex.main.exception.BusinessException;
import hu.finex.main.model.enums.TicketStatus;
import hu.finex.main.service.SupportTicketService;

@ActiveProfiles("test")
@WebMvcTest(
    controllers = SupportTicketController.class,
    excludeFilters = {
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = hu.finex.main.config.SecurityConfig.class),
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = hu.finex.main.security.JwtAuthenticationFilter.class)
    }
)
@AutoConfigureMockMvc(addFilters = false)
class SupportTicketControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockBean SupportTicketService supportTicketService;

    private SupportTicketResponse sampleResponse() {
        return SupportTicketResponse.builder()
                .id(1501L)
                .userId(42L)
                .userFullName("Kovács Anna")
                .title("Kártya tiltás")
                .message("Elvesztettem a kártyámat")
                .status(TicketStatus.IN_PROGRESS)
                .messages(List.of(TicketMessageResponse.builder()
                        .id(1L)
                        .authorName("FineX Ügyfélszolgálat")
                        .staff(true)
                        .message("A kártyát letiltottuk.")
                        .createdAt(Instant.parse("2025-03-10T10:00:00Z"))
                        .build()))
                .build();
    }

    @Test
    void create_shouldReturn201_andBody() throws Exception {
        CreateSupportTicketRequest req = CreateSupportTicketRequest.builder()
                .title("Kártya tiltás")
                .message("Elvesztettem a kártyámat")
                .build();

        when(supportTicketService.create(any(CreateSupportTicketRequest.class))).thenReturn(sampleResponse());

        mockMvc.perform(post("/support-tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1501));
    }

    @Test
    void create_shouldReturn400_whenInvalidBody() throws Exception {
        CreateSupportTicketRequest req = CreateSupportTicketRequest.builder().build();

        mockMvc.perform(post("/support-tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(supportTicketService);
    }

    @Test
    void create_shouldReturn400_whenOpenTicketAlreadyExists() throws Exception {
        CreateSupportTicketRequest req = CreateSupportTicketRequest.builder()
                .title("Még egy")
                .message("Kérdés")
                .build();

        when(supportTicketService.create(any(CreateSupportTicketRequest.class))).thenThrow(new BusinessException("Már van egy nyitott ticketed, kérjük várd meg a választ."));

        mockMvc.perform(post("/support-tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Már van egy nyitott ticketed, kérjük várd meg a választ."));
    }

    @Test
    void listMine_shouldReturn200_andPage() throws Exception {
        SupportTicketListItemResponse item = SupportTicketListItemResponse.builder()
                .id(1501L)
                .title("Kártya tiltás")
                .status(TicketStatus.OPEN)
                .build();

        when(supportTicketService.listMine(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(item), PageRequest.of(0, 20), 1));

        mockMvc.perform(get("/support-tickets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].status").value("OPEN"));
    }

    @Test
    void getById_shouldReturn200_withConversation() throws Exception {
        when(supportTicketService.getMine(1501L)).thenReturn(sampleResponse());

        mockMvc.perform(get("/support-tickets/1501"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.messages.length()").value(1))
                .andExpect(jsonPath("$.messages[0].staff").value(true));
    }

    @Test
    void addMessage_shouldReturn200() throws Exception {
        CreateTicketMessageRequest req = CreateTicketMessageRequest.builder().message("Köszönöm!").build();

        when(supportTicketService.addMessage(eq(1501L), any(CreateTicketMessageRequest.class))).thenReturn(sampleResponse());

        mockMvc.perform(post("/support-tickets/1501/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1501));
    }

    @Test
    void addMessage_shouldReturn400_whenMessageIsBlank() throws Exception {
        CreateTicketMessageRequest req = CreateTicketMessageRequest.builder().message(" ").build();

        mockMvc.perform(post("/support-tickets/1501/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(supportTicketService);
    }
}
