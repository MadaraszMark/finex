package hu.finex.main.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

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
import hu.finex.main.service.CategoryService;

@ActiveProfiles("test")
@WebMvcTest(
    controllers = CategoryController.class,
    excludeFilters = {
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = hu.finex.main.config.SecurityConfig.class),
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = hu.finex.main.security.JwtAuthenticationFilter.class)
    }
)
@AutoConfigureMockMvc(addFilters = false)
class CategoryControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockBean CategoryService categoryService;

    @Test
    void create_shouldReturn201_andBody() throws Exception {
        CreateCategoryRequest req = CreateCategoryRequest.builder()
                .name("Food")
                .icon("🍔")
                .build();

        CategoryResponse resp = CategoryResponse.builder()
                .id(12L)
                .name("Food")
                .icon("🍔")
                .build();

        when(categoryService.create(any(CreateCategoryRequest.class))).thenReturn(resp);

        mockMvc.perform(post("/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(12))
                .andExpect(jsonPath("$.name").value("Food"))
                .andExpect(jsonPath("$.icon").value("🍔"));
    }

    @Test
    void create_shouldReturn400_whenNameMissing() throws Exception {
        CreateCategoryRequest req = CreateCategoryRequest.builder()
                .icon("🍔")
                .build();

        mockMvc.perform(post("/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getById_shouldReturn200_andBody() throws Exception {
        CategoryResponse resp = CategoryResponse.builder()
                .id(10L)
                .name("Bills")
                .icon("💡")
                .build();

        when(categoryService.getById(10L)).thenReturn(resp);

        mockMvc.perform(get("/categories/10"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.name").value("Bills"))
                .andExpect(jsonPath("$.icon").value("💡"));
    }

    @Test
    void listAll_shouldReturn200_andList() throws Exception {
        when(categoryService.listAll()).thenReturn(List.of(
                CategoryResponse.builder().id(1L).name("Food").icon("🍕").build(),
                CategoryResponse.builder().id(2L).name("Transport").icon("🚗").build()
        ));

        mockMvc.perform(get("/categories"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Food"))
                .andExpect(jsonPath("$[1].name").value("Transport"));
    }

    @Test
    void delete_shouldReturn204() throws Exception {
        doNothing().when(categoryService).delete(eq(10L));

        mockMvc.perform(delete("/categories/10"))
                .andExpect(status().isNoContent());
    }
}
