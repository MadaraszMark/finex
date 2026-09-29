package hu.finex.main.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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

import hu.finex.main.dto.CategoryResponse;
import hu.finex.main.dto.CreateCategoryRequest;
import hu.finex.main.dto.UpdateCategoryRequest;
import hu.finex.main.exception.BusinessException;
import hu.finex.main.service.CategoryService;

@ActiveProfiles("test")
@WebMvcTest(
    controllers = AdminCategoryController.class,
    excludeFilters = {
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = hu.finex.main.config.SecurityConfig.class),
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = hu.finex.main.security.JwtAuthenticationFilter.class)
    }
)
@AutoConfigureMockMvc(addFilters = false)
class AdminCategoryControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockBean CategoryService categoryService;

    @Test
    void create_shouldReturn201_andBody() throws Exception {
        CreateCategoryRequest req = CreateCategoryRequest.builder()
                .name("Oktatás")
                .icon("graduation-cap")
                .build();

        when(categoryService.create(any(CreateCategoryRequest.class))).thenReturn(CategoryResponse.builder().id(14L).name("Oktatás").icon("graduation-cap").build());

        mockMvc.perform(post("/admin/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(14));
    }

    @Test
    void create_shouldReturn400_whenNameMissing() throws Exception {
        CreateCategoryRequest req = CreateCategoryRequest.builder().icon("x").build();

        mockMvc.perform(post("/admin/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(categoryService);
    }

    @Test
    void update_shouldReturn200() throws Exception {
        UpdateCategoryRequest req = UpdateCategoryRequest.builder()
                .name("Egyéb kiadás")
                .icon("circle-ellipsis")
                .build();

        when(categoryService.update(eq(13L), any(UpdateCategoryRequest.class))).thenReturn(CategoryResponse.builder().id(13L).name("Egyéb kiadás").build());

        mockMvc.perform(put("/admin/categories/13")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Egyéb kiadás"));
    }

    @Test
    void delete_shouldReturn204() throws Exception {
        mockMvc.perform(delete("/admin/categories/13")).andExpect(status().isNoContent());

        verify(categoryService).delete(13L);
    }

    @Test
    void delete_shouldReturn400_whenCategoryIsInUse() throws Exception {
        doThrow(new BusinessException("A kategória használatban van, ezért nem törölhető.")).when(categoryService).delete(1L);

        mockMvc.perform(delete("/admin/categories/1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("A kategória használatban van, ezért nem törölhető."));
    }
}
