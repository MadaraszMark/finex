package hu.finex.main.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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

import hu.finex.main.dto.CreateTicketMessageRequest;
import hu.finex.main.dto.SupportTicketListItemResponse;
import hu.finex.main.dto.SupportTicketResponse;
import hu.finex.main.dto.UpdateSupportTicketStatusRequest;
import hu.finex.main.model.enums.TicketStatus;
import hu.finex.main.service.SupportTicketService;

@ActiveProfiles("test")
@WebMvcTest(
    controllers = AdminSupportTicketController.class,
    excludeFilters = {
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = hu.finex.main.config.SecurityConfig.class),
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = hu.finex.main.security.JwtAuthenticationFilter.class)
    }
)
@AutoConfigureMockMvc(addFilters = false)
class AdminSupportTicketControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockBean SupportTicketService supportTicketService;

    @Test
    void listAll_shouldReturn200_withoutStatusFilter() throws Exception {
        SupportTicketListItemResponse item = SupportTicketListItemResponse.builder()
                .id(1501L)
                .title("Kártya tiltás")
                .userFullName("Kovács Anna")
                .status(TicketStatus.OPEN)
                .build();

        when(supportTicketService.listAll(isNull(), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(item), PageRequest.of(0, 20), 1));

        mockMvc.perform(get("/admin/support-tickets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].userFullName").value("Kovács Anna"));
    }

    @Test
    void getById_shouldReturn200() throws Exception {
        when(supportTicketService.getById(1501L)).thenReturn(SupportTicketResponse.builder().id(1501L).status(TicketStatus.OPEN).messages(List.of()).build());

        mockMvc.perform(get("/admin/support-tickets/1501"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1501));
    }

    @Test
    void reply_shouldReturn200() throws Exception {
        CreateTicketMessageRequest req = CreateTicketMessageRequest.builder().message("A kártyát letiltottuk.").build();

        when(supportTicketService.reply(eq(1501L), any(CreateTicketMessageRequest.class)))
                .thenReturn(SupportTicketResponse.builder().id(1501L).status(TicketStatus.IN_PROGRESS).messages(List.of()).build());

        mockMvc.perform(post("/admin/support-tickets/1501/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }

    @Test
    void updateStatus_shouldReturn200() throws Exception {
        UpdateSupportTicketStatusRequest req = UpdateSupportTicketStatusRequest.builder().status(TicketStatus.RESOLVED).build();

        when(supportTicketService.updateStatus(eq(1501L), any(UpdateSupportTicketStatusRequest.class)))
                .thenReturn(SupportTicketResponse.builder().id(1501L).status(TicketStatus.RESOLVED).messages(List.of()).build());

        mockMvc.perform(patch("/admin/support-tickets/1501/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RESOLVED"));
    }
}
