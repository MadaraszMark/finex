package hu.finex.main.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
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
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import hu.finex.main.dto.BeneficiaryResponse;
import hu.finex.main.dto.CreateBeneficiaryRequest;
import hu.finex.main.dto.UpdateBeneficiaryRequest;
import hu.finex.main.service.BeneficiaryService;

@ActiveProfiles("test")
@WebMvcTest(
    controllers = BeneficiaryController.class,
    excludeFilters = {
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = hu.finex.main.config.SecurityConfig.class),
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = hu.finex.main.security.JwtAuthenticationFilter.class)
    }
)
@AutoConfigureMockMvc(addFilters = false)
class BeneficiaryControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockBean BeneficiaryService beneficiaryService;

    private BeneficiaryResponse sample() {
        return BeneficiaryResponse.builder()
                .id(4L)
                .name("Nagy Bence")
                .accountNumber("HU28104000950000521700000003")
                .note("Kolléga")
                .internal(true)
                .build();
    }

    @Test
    void listMine_shouldReturn200() throws Exception {
        when(beneficiaryService.listMine()).thenReturn(List.of(sample()));

        mockMvc.perform(get("/beneficiaries"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].internal").value(true));
    }

    @Test
    void create_shouldReturn201() throws Exception {
        CreateBeneficiaryRequest req = CreateBeneficiaryRequest.builder()
                .name("Nagy Bence")
                .accountNumber("HU28 1040 0095 0000 5217 0000 0003")
                .note("Kolléga")
                .build();

        when(beneficiaryService.create(any(CreateBeneficiaryRequest.class))).thenReturn(sample());

        mockMvc.perform(post("/beneficiaries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accountNumber").value("HU28104000950000521700000003"));
    }

    @Test
    void create_shouldReturn400_whenNameMissing() throws Exception {
        CreateBeneficiaryRequest req = CreateBeneficiaryRequest.builder()
                .accountNumber("HU28104000950000521700000003")
                .build();

        mockMvc.perform(post("/beneficiaries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.violations[0].field").value("name"));

        verifyNoInteractions(beneficiaryService);
    }

    @Test
    void update_shouldReturn200() throws Exception {
        UpdateBeneficiaryRequest req = UpdateBeneficiaryRequest.builder()
                .name("Nagy Bence")
                .accountNumber("HU28104000950000521700000003")
                .build();

        when(beneficiaryService.update(eq(4L), any(UpdateBeneficiaryRequest.class))).thenReturn(sample());

        mockMvc.perform(put("/beneficiaries/4")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    void delete_shouldReturn204() throws Exception {
        mockMvc.perform(delete("/beneficiaries/4")).andExpect(status().isNoContent());

        verify(beneficiaryService).delete(4L);
    }
}
