package hu.finex.main.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;
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

import hu.finex.main.dto.LoginLogResponse;
import hu.finex.main.exception.NotFoundException;
import hu.finex.main.model.enums.LoginStatus;
import hu.finex.main.service.LoginLogService;

@ActiveProfiles("test")
@WebMvcTest(
    controllers = AdminLoginLogController.class,
    excludeFilters = {
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = hu.finex.main.config.SecurityConfig.class),
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = hu.finex.main.security.JwtAuthenticationFilter.class)
    }
)
@AutoConfigureMockMvc(addFilters = false)
class AdminLoginLogControllerTest {

    @Autowired MockMvc mockMvc;

    @MockBean LoginLogService loginLogService;

    @Test
    void search_shouldPassAllFilters() throws Exception {
        LoginLogResponse item = LoginLogResponse.builder()
                .id(5501L)
                .email("anna@finex.com")
                .status(LoginStatus.FAILED)
                .ipAddress("192.168.1.11")
                .failureReason("Ismeretlen e-mail cím")
                .build();

        when(loginLogService.search(eq(LoginStatus.FAILED), eq(42L), eq("192.168.1.11"), eq(LocalDate.of(2025, 3, 1)), eq(LocalDate.of(2025, 3, 31)), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(item), PageRequest.of(0, 20), 1));

        mockMvc.perform(get("/admin/login-logs")
                        .param("status", "FAILED")
                        .param("userId", "42")
                        .param("ipAddress", "192.168.1.11")
                        .param("from", "2025-03-01")
                        .param("to", "2025-03-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].failureReason").value("Ismeretlen e-mail cím"));
    }

    @Test
    void search_shouldWorkWithoutFilters() throws Exception {
        when(loginLogService.search(isNull(), isNull(), isNull(), isNull(), isNull(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 20), 0));

        mockMvc.perform(get("/admin/login-logs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(0));
    }

    @Test
    void getById_shouldReturn404_whenMissing() throws Exception {
        when(loginLogService.getById(1L)).thenThrow(new NotFoundException("Login log nem található."));

        mockMvc.perform(get("/admin/login-logs/1"))
                .andExpect(status().isNotFound());
    }
}
