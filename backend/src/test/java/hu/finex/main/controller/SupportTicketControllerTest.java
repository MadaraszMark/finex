package hu.finex.main.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.security.Principal;
import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import hu.finex.main.dto.CreateSupportTicketRequest;
import hu.finex.main.dto.SupportTicketResponse;
import hu.finex.main.dto.UpdateSupportTicketStatusRequest;
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

    private SupportTicketResponse sampleTicket() {
        return SupportTicketResponse.builder()
                .id(1501L)
                .userId(42L)
                .title("Probléma történt a legutóbbi utalásomnál..")
                .message("Az utalásom nem érkezett meg...")
                .status(TicketStatus.OPEN)
                .createdAt(Instant.parse("2025-02-12T12:10:05Z"))
                .updatedAt(Instant.parse("2025-02-12T12:30:10Z"))
                .build();
    }

    @Test
    void create_shouldReturn201_andBody() throws Exception {
        Principal principal = () -> "me@finex.hu";

        CreateSupportTicketRequest req = CreateSupportTicketRequest.builder()
                .title("Probléma történt a legutóbbi utalásomnál..")
                .message("Az utalásom nem érkezett meg...")
                .build();

        when(supportTicketService.create(any(CreateSupportTicketRequest.class), any(Principal.class)))
                .thenReturn(sampleTicket());

        mockMvc.perform(post("/support-tickets")
                        .principal(principal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1501))
                .andExpect(jsonPath("$.status").value("OPEN"));
    }

    @Test
    void create_shouldReturn400_whenInvalidBody() throws Exception {
        Principal principal = () -> "me@finex.hu";
        CreateSupportTicketRequest req = CreateSupportTicketRequest.builder().build();

        mockMvc.perform(post("/support-tickets")
                        .principal(principal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getById_shouldReturn200_andBody() throws Exception {
        when(supportTicketService.getById(1501L)).thenReturn(sampleTicket());

        mockMvc.perform(get("/support-tickets/1501"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1501))
                .andExpect(jsonPath("$.userId").value(42))
                .andExpect(jsonPath("$.status").value("OPEN"));
    }

    @Test
    void listByUser_shouldReturn200_andPage() throws Exception {
        Page<SupportTicketResponse> page =
                new PageImpl<>(List.of(sampleTicket()), PageRequest.of(0, 10), 1);

        when(supportTicketService.listByUser(eq(42L), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/support-tickets/user/42")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id").value(1501));
    }

    @Test
    void listByStatus_shouldReturn200_andPage() throws Exception {
        Page<SupportTicketResponse> page =
                new PageImpl<>(List.of(sampleTicket()), PageRequest.of(0, 10), 1);

        when(supportTicketService.listByStatus(eq(TicketStatus.OPEN), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/support-tickets/status/OPEN")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].status").value("OPEN"));
    }

    @Test
    void listByUserAndStatus_shouldReturn200_andPage() throws Exception {
        Page<SupportTicketResponse> page =
                new PageImpl<>(List.of(sampleTicket()), PageRequest.of(0, 10), 1);

        when(supportTicketService.listByUserAndStatus(eq(42L), eq(TicketStatus.OPEN), any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/support-tickets/user/42/status/OPEN")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].userId").value(42));
    }

    @Test
    void updateStatus_shouldReturn200_andBody() throws Exception {
        UpdateSupportTicketStatusRequest req = UpdateSupportTicketStatusRequest.builder()
                .status(TicketStatus.IN_PROGRESS)
                .build();

        SupportTicketResponse updated = SupportTicketResponse.builder()
                .id(1501L)
                .userId(42L)
                .title("Probléma történt a legutóbbi utalásomnál..")
                .message("Az utalásom nem érkezett meg...")
                .status(TicketStatus.IN_PROGRESS)
                .createdAt(Instant.parse("2025-02-12T12:10:05Z"))
                .updatedAt(Instant.parse("2025-02-12T12:30:10Z"))
                .build();

        when(supportTicketService.updateStatus(eq(1501L), any(UpdateSupportTicketStatusRequest.class)))
                .thenReturn(updated);

        mockMvc.perform(patch("/support-tickets/1501/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1501))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }

    @Test
    void updateStatus_shouldReturn400_whenInvalidBody() throws Exception {
        UpdateSupportTicketStatusRequest req = UpdateSupportTicketStatusRequest.builder().build();

        mockMvc.perform(patch("/support-tickets/1501/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }
}
