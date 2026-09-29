package hu.finex.main.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
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

import hu.finex.main.dto.LoginLogListItemResponse;
import hu.finex.main.model.enums.LoginStatus;
import hu.finex.main.service.LoginLogService;

@ActiveProfiles("test")
@WebMvcTest(
    controllers = LoginLogController.class,
    excludeFilters = {
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = hu.finex.main.config.SecurityConfig.class),
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = hu.finex.main.security.JwtAuthenticationFilter.class)
    }
)
@AutoConfigureMockMvc(addFilters = false)
class LoginLogControllerTest {

    @Autowired MockMvc mockMvc;

    @MockBean LoginLogService loginLogService;

    @Test
    void listMine_shouldReturn200_andPage() throws Exception {
        List<LoginLogListItemResponse> items = List.of(
                LoginLogListItemResponse.builder()
                        .status(LoginStatus.SUCCESS)
                        .ipAddress("192.168.1.10")
                        .userAgent("Mozilla/5.0")
                        .createdAt(Instant.parse("2025-03-01T08:00:00Z"))
                        .build(),
                LoginLogListItemResponse.builder()
                        .status(LoginStatus.FAILED)
                        .ipAddress("192.168.1.11")
                        .failureReason("Hibás jelszó")
                        .createdAt(Instant.parse("2025-02-28T21:00:00Z"))
                        .build()
        );

        when(loginLogService.listMine(any(Pageable.class))).thenReturn(new PageImpl<>(items, PageRequest.of(0, 20), items.size()));

        mockMvc.perform(get("/login-logs/me").param("page", "0").param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].status").value("SUCCESS"))
                .andExpect(jsonPath("$.content[1].failureReason").value("Hibás jelszó"));

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(loginLogService).listMine(pageableCaptor.capture());
        assertEquals(5, pageableCaptor.getValue().getPageSize());
    }
}
