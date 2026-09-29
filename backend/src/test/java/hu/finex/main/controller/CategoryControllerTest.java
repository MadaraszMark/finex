package hu.finex.main.controller;

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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import hu.finex.main.dto.CategoryResponse;
import hu.finex.main.exception.NotFoundException;
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

    @MockBean CategoryService categoryService;

    @Test
    void getById_shouldReturn200_andBody() throws Exception {
        CategoryResponse resp = CategoryResponse.builder()
                .id(10L)
                .name("Élelmiszer")
                .icon("shopping-cart")
                .build();

        when(categoryService.getById(10L)).thenReturn(resp);

        mockMvc.perform(get("/categories/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.name").value("Élelmiszer"))
                .andExpect(jsonPath("$.icon").value("shopping-cart"));
    }

    @Test
    void getById_shouldReturn404_whenMissing() throws Exception {
        when(categoryService.getById(99L)).thenThrow(new NotFoundException("Kategória nem található."));

        mockMvc.perform(get("/categories/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Kategória nem található."));
    }

    @Test
    void listAll_shouldReturn200_andList() throws Exception {
        when(categoryService.listAll()).thenReturn(List.of(
                CategoryResponse.builder().id(1L).name("Ajándék").icon("gift").build(),
                CategoryResponse.builder().id(2L).name("Élelmiszer").icon("shopping-cart").build()
        ));

        mockMvc.perform(get("/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Ajándék"));
    }

    @Test
    void create_shouldNotBeAvailableOnPublicEndpoint() throws Exception {
        // Kategóriát csak az admin hozhat létre (/admin/categories)
        mockMvc.perform(post("/categories"))
                .andExpect(status().isMethodNotAllowed());
    }
}
