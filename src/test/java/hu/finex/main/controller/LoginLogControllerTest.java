package hu.finex.main.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.Instant;
import java.time.OffsetDateTime;
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

import hu.finex.main.dto.LoginLogListItemResponse;
import hu.finex.main.dto.LoginLogResponse;
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
    void getById_shouldReturn200_andBody() throws Exception {
        LoginLogResponse resp = LoginLogResponse.builder()
                .id(5501L)
                .userId(42L)
                .status(LoginStatus.FAILED)
                .ipAddress("192.168.1.11")
                .userAgent("Mozilla/5.0")
                .failureReason("Invalid password")
                .createdAt(Instant.parse("2025-02-12T14:22:10Z"))
                .build();

        when(loginLogService.getById(5501L)).thenReturn(resp);

        mockMvc.perform(get("/login-logs/5501"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(5501))
                .andExpect(jsonPath("$.userId").value(42))
                .andExpect(jsonPath("$.status").value("FAILED"))
                .andExpect(jsonPath("$.ipAddress").value("192.168.1.11"))
                .andExpect(jsonPath("$.failureReason").value("Invalid password"));
    }

    @Test
    void listByUser_shouldReturn200_andPage() throws Exception {
        List<LoginLogListItemResponse> items = List.of(
                LoginLogListItemResponse.builder()
                        .status(LoginStatus.SUCCESS)
                        .ipAddress("10.0.0.5")
                        .createdAt(Instant.parse("2025-02-12T14:22:10Z"))
                        .build(),
                LoginLogListItemResponse.builder()
                        .status(LoginStatus.FAILED)
                        .ipAddress("10.0.0.6")
                        .createdAt(Instant.parse("2025-02-13T14:22:10Z"))
                        .build()
        );

        Page<LoginLogListItemResponse> page = new PageImpl<>(items, PageRequest.of(0, 20), items.size());

        when(loginLogService.listByUser(eq(42L), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/login-logs/user/42")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].status").value("SUCCESS"))
                .andExpect(jsonPath("$.content[1].status").value("FAILED"));
    }

    @Test
    void listByStatus_shouldReturn200_andPage() throws Exception {
        List<LoginLogListItemResponse> items = List.of(
                LoginLogListItemResponse.builder()
                        .status(LoginStatus.SUCCESS)
                        .ipAddress("10.0.0.5")
                        .createdAt(Instant.parse("2025-02-12T14:22:10Z"))
                        .build()
        );

        Page<LoginLogListItemResponse> page = new PageImpl<>(items, PageRequest.of(0, 10), 1);

        when(loginLogService.listByStatus(eq(LoginStatus.SUCCESS), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/login-logs/status/SUCCESS")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].ipAddress").value("10.0.0.5"));
    }

    @Test
    void listByIp_shouldReturn200_andPage() throws Exception {
        List<LoginLogListItemResponse> items = List.of(
                LoginLogListItemResponse.builder()
                        .status(LoginStatus.FAILED)
                        .ipAddress("192.168.1.11")
                        .createdAt(Instant.parse("2025-02-12T14:22:10Z"))
                        .build()
        );

        Page<LoginLogListItemResponse> page = new PageImpl<>(items, PageRequest.of(0, 10), 1);

        when(loginLogService.listByIp(eq("192.168.1.11"), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/login-logs/ip/192.168.1.11")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].status").value("FAILED"));
    }

    @Test
    void listByUserAndStatus_shouldReturn200_andPage() throws Exception {
        List<LoginLogListItemResponse> items = List.of(
                LoginLogListItemResponse.builder()
                        .status(LoginStatus.FAILED)
                        .ipAddress("10.0.0.6")
                        .createdAt(Instant.parse("2025-02-13T14:22:10Z"))
                        .build()
        );

        Page<LoginLogListItemResponse> page = new PageImpl<>(items, PageRequest.of(0, 10), 1);

        when(loginLogService.listByUserAndStatus(eq(42L), eq(LoginStatus.FAILED), any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/login-logs/user/42/status/FAILED")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].ipAddress").value("10.0.0.6"));
    }

    @Test
    void listByDateRange_shouldReturn200_andPage() throws Exception {
        OffsetDateTime start = OffsetDateTime.parse("2026-01-01T00:00:00+01:00");
        OffsetDateTime end   = OffsetDateTime.parse("2026-01-10T00:00:00+01:00");

        List<LoginLogListItemResponse> items = List.of(
                LoginLogListItemResponse.builder()
                        .status(LoginStatus.SUCCESS)
                        .ipAddress("10.0.0.5")
                        .createdAt(Instant.parse("2025-02-12T14:22:10Z"))
                        .build()
        );

        Page<LoginLogListItemResponse> page = new PageImpl<>(items, PageRequest.of(0, 10), 1);

        when(loginLogService.listByDateRange(eq(start), eq(end), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/login-logs/range")
                        .param("start", start.toString())
                        .param("end", end.toString())
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].status").value("SUCCESS"));
    }
}
