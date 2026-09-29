package hu.finex.main.controller;

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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import hu.finex.main.dto.TransactionCategoryListItemResponse;
import hu.finex.main.dto.TransactionCategoryResponse;
import hu.finex.main.exception.BusinessException;
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

    @MockBean TransactionCategoryService transactionCategoryService;

    @Test
    void assign_shouldReturn200_andBody() throws Exception {
        TransactionCategoryResponse resp = TransactionCategoryResponse.builder()
                .id(120L)
                .transactionId(5012L)
                .categoryId(3L)
                .categoryName("Közlekedés")
                .categoryIcon("bus")
                .build();

        when(transactionCategoryService.assignCategory(5012L, 3L)).thenReturn(resp);

        mockMvc.perform(post("/transaction-categories/5012/assign/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(120))
                .andExpect(jsonPath("$.categoryName").value("Közlekedés"));
    }

    @Test
    void assign_shouldReturn400_whenAlreadyAssigned() throws Exception {
        when(transactionCategoryService.assignCategory(5012L, 3L)).thenThrow(new BusinessException("A kategória már hozzá van rendelve a tranzakcióhoz."));

        mockMvc.perform(post("/transaction-categories/5012/assign/3"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void listByTransaction_shouldReturn200_andList() throws Exception {
        when(transactionCategoryService.listByTransaction(5012L)).thenReturn(List.of(
                TransactionCategoryListItemResponse.builder().categoryId(3L).categoryName("Közlekedés").categoryIcon("bus").build(),
                TransactionCategoryListItemResponse.builder().categoryId(4L).categoryName("Rezsi").categoryIcon("zap").build()
        ));

        mockMvc.perform(get("/transaction-categories/transaction/5012"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[1].categoryName").value("Rezsi"));
    }

    @Test
    void delete_shouldReturn204() throws Exception {
        mockMvc.perform(delete("/transaction-categories/120"))
                .andExpect(status().isNoContent());

        verify(transactionCategoryService).deleteRelation(120L);
    }
}
