package hu.finex.main.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import hu.finex.main.dto.NotificationResponse;
import hu.finex.main.dto.UnreadCountResponse;
import hu.finex.main.model.enums.NotificationType;
import hu.finex.main.service.NotificationService;

@ActiveProfiles("test")
@WebMvcTest(
    controllers = NotificationController.class,
    excludeFilters = {
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = hu.finex.main.config.SecurityConfig.class),
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = hu.finex.main.security.JwtAuthenticationFilter.class)
    }
)
@AutoConfigureMockMvc(addFilters = false)
class NotificationControllerTest {

    @Autowired MockMvc mockMvc;

    @MockBean NotificationService notificationService;

    @Test
    void listMine_shouldReturn200_andPage() throws Exception {
        NotificationResponse item = NotificationResponse.builder()
                .id(1L)
                .type(NotificationType.TRANSACTION)
                .title("Beérkező utalás")
                .message("5 000 Ft érkezett.")
                .read(false)
                .build();

        when(notificationService.listMine(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(item), PageRequest.of(0, 20), 1));

        mockMvc.perform(get("/notifications"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("Beérkező utalás"))
                .andExpect(jsonPath("$.content[0].read").value(false));
    }

    @Test
    void countUnread_shouldReturn200() throws Exception {
        when(notificationService.countUnread()).thenReturn(UnreadCountResponse.builder().count(3).build());

        mockMvc.perform(get("/notifications/unread-count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(3));
    }

    @Test
    void markAsRead_shouldReturn200() throws Exception {
        when(notificationService.markAsRead(1L)).thenReturn(NotificationResponse.builder().id(1L).read(true).build());

        mockMvc.perform(patch("/notifications/1/read"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.read").value(true));
    }

    @Test
    void markAllAsRead_shouldReturn204() throws Exception {
        mockMvc.perform(patch("/notifications/read-all")).andExpect(status().isNoContent());

        verify(notificationService).markAllAsRead();
    }
}
