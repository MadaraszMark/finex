package hu.finex.main.controller;

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

import hu.finex.main.dto.TransactionCategoryListItemResponse;
import hu.finex.main.dto.TransactionCategoryResponse;
import hu.finex.main.service.TransactionCategoryService;

@ActiveProfiles("test")
@WebMvcTest(
    controllers = TransactionCategoryController.class,
    excludeFilters = {
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = hu.finex.main.config.SecurityConfig.class),
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = hu.finex.main.security.JwtAuthenticationFilter.class)
    }
)
@AutoConfigureMockMvc(addFilters = false)
class TransactionCategoryControllerTest {

    @Autowired MockMvc mockMvc;

    @MockBean TransactionCategoryService service;

    @Test
    void assign_shouldReturn200_andBody() throws Exception {
        TransactionCategoryResponse resp = TransactionCategoryResponse.builder()
                .id(120L)
                .transactionId(5012L)
                .categoryId(3L)
                .categoryName("Food")
                .categoryIcon("🍔")
                .build();

        when(service.assignCategory(eq(5012L), eq(3L))).thenReturn(resp);

        mockMvc.perform(post("/transaction-categories/5012/assign/3"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(120))
                .andExpect(jsonPath("$.transactionId").value(5012))
                .andExpect(jsonPath("$.categoryId").value(3))
                .andExpect(jsonPath("$.categoryName").value("Food"))
                .andExpect(jsonPath("$.categoryIcon").value("🍔"));
    }

    @Test
    void listByTransaction_shouldReturn200_andList() throws Exception {
        List<TransactionCategoryListItemResponse> list = List.of(
                TransactionCategoryListItemResponse.builder()
                        .categoryId(3L)
                        .categoryName("Shopping")
                        .categoryIcon("🛍️")
                        .build(),
                TransactionCategoryListItemResponse.builder()
                        .categoryId(7L)
                        .categoryName("Bills")
                        .categoryIcon("💡")
                        .build()
        );

        when(service.listByTransaction(eq(5012L))).thenReturn(list);

        mockMvc.perform(get("/transaction-categories/transaction/5012"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].categoryId").value(3))
                .andExpect(jsonPath("$[0].categoryName").value("Shopping"))
                .andExpect(jsonPath("$[0].categoryIcon").value("🛍️"))
                .andExpect(jsonPath("$[1].categoryId").value(7));
    }

    @Test
    void listByCategory_shouldReturn200_andList() throws Exception {
        List<TransactionCategoryResponse> list = List.of(
                TransactionCategoryResponse.builder()
                        .id(120L)
                        .transactionId(5012L)
                        .categoryId(3L)
                        .categoryName("Food")
                        .categoryIcon("🍔")
                        .build(),
                TransactionCategoryResponse.builder()
                        .id(121L)
                        .transactionId(5013L)
                        .categoryId(3L)
                        .categoryName("Food")
                        .categoryIcon("🍔")
                        .build()
        );

        when(service.listByCategory(eq(3L))).thenReturn(list);

        mockMvc.perform(get("/transaction-categories/category/3"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(120))
                .andExpect(jsonPath("$[0].transactionId").value(5012))
                .andExpect(jsonPath("$[0].categoryId").value(3))
                .andExpect(jsonPath("$[0].categoryName").value("Food"))
                .andExpect(jsonPath("$[0].categoryIcon").value("🍔"))
                .andExpect(jsonPath("$[1].id").value(121));
    }

    @Test
    void delete_shouldReturn204() throws Exception {
        doNothing().when(service).deleteRelation(eq(120L));

        mockMvc.perform(delete("/transaction-categories/120"))
                .andExpect(status().isNoContent());
    }
}
