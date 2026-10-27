package hu.finex.main.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
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

import hu.finex.main.dto.AuditLogResponse;
import hu.finex.main.model.enums.AuditAction;
import hu.finex.main.model.enums.AuditTargetType;
import hu.finex.main.service.AuditService;

@ActiveProfiles("test")
@WebMvcTest(
    controllers = AdminAuditLogController.class,
    excludeFilters = {
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = hu.finex.main.config.SecurityConfig.class),
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = hu.finex.main.security.JwtAuthenticationFilter.class)
    }
)
@AutoConfigureMockMvc(addFilters = false)
class AdminAuditLogControllerTest {

    @Autowired MockMvc mockMvc;

    @MockBean AuditService auditService;

    @Test
    void search_shouldPassFilters_andReturnPage() throws Exception {
        AuditLogResponse item = AuditLogResponse.builder()
                .id(12L)
                .actorEmail("admin@finex.hu")
                .action(AuditAction.USER_STATUS_CHANGED)
                .targetType(AuditTargetType.USER)
                .targetId(42L)
                .build();
        when(auditService.search(eq(AuditAction.USER_STATUS_CHANGED), isNull(), eq(LocalDate.of(2026, 10, 1)), isNull(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(item), PageRequest.of(0, 50), 1));

        mockMvc.perform(get("/admin/audit-log").param("action", "USER_STATUS_CHANGED").param("from", "2026-10-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].actorEmail").value("admin@finex.hu"))
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.page.totalElements").value(1));
    }

    // A spring.data.web.pageable.max-page-size miatt legfeljebb 100 elem jön
    @Test
    void search_shouldCapPageSize() throws Exception {
        when(auditService.search(isNull(), isNull(), isNull(), isNull(), any(Pageable.class)))
                .thenAnswer(invocation -> new PageImpl<AuditLogResponse>(List.of(), invocation.getArgument(4), 0));

        mockMvc.perform(get("/admin/audit-log").param("size", "100000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page.size").value(100));

        verify(auditService).search(isNull(), isNull(), isNull(), isNull(), argThat(pageable -> pageable.getPageSize() == 100));
    }

    @Test
    void search_shouldReturn400_forUnknownAction() throws Exception {
        mockMvc.perform(get("/admin/audit-log").param("action", "HACKED"))
                .andExpect(status().isBadRequest());
    }
}
