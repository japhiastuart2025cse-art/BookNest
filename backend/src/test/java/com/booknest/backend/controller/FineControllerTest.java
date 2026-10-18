package com.booknest.backend.controller;

import com.booknest.backend.dto.FinesSummaryDTO;
import com.booknest.backend.exception.FineNotFoundException;
import com.booknest.backend.exception.GlobalExceptionHandler;
import com.booknest.backend.model.Fine;
import com.booknest.backend.service.FineService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class FineControllerTest {

    private MockMvc mockMvc;

    @Mock
    private FineService fineService;

    @InjectMocks
    private FineController fineController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(fineController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("GET /api/fines returns 200 OK with list of all fines")
    void testGetAllFines() throws Exception {
        Fine fine1 = new Fine(1, 101, 1, 20.0, 4, "PENDING", null, null);
        Fine fine2 = new Fine(2, 102, 2, 40.0, 8, "PAID", LocalDate.now(), "Paid at desk");

        when(fineService.getAllFines()).thenReturn(List.of(fine1, fine2));

        mockMvc.perform(get("/api/fines"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].fineId").value(1))
                .andExpect(jsonPath("$[0].amount").value(20.0))
                .andExpect(jsonPath("$[1].fineId").value(2))
                .andExpect(jsonPath("$[1].status").value("PAID"));
    }

    @Test
    @DisplayName("GET /api/fines/summary returns 200 OK with ledger summary")
    void testGetFinesSummary() throws Exception {
        FinesSummaryDTO summary = new FinesSummaryDTO(75.0, 5.0, 0, 3, 140.0);
        when(fineService.getFinesSummary()).thenReturn(summary);

        mockMvc.perform(get("/api/fines/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pendingTotal").value(75.0))
                .andExpect(jsonPath("$.dailyRate").value(5.0))
                .andExpect(jsonPath("$.defaultersCount").value(3))
                .andExpect(jsonPath("$.totalSettled").value(140.0));
    }

    @Test
    @DisplayName("POST /api/fines/{id}/pay returns 200 OK when fine paid")
    void testPayFine_Success() throws Exception {
        Fine settled = new Fine(1, 101, 1, 20.0, 4, "PAID", LocalDate.now(), "Cash desk");
        when(fineService.payFine(eq(1), any())).thenReturn(settled);

        String json = """
                {
                    "paymentNote": "Cash desk"
                }
                """;

        mockMvc.perform(post("/api/fines/1/pay")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fineId").value(1))
                .andExpect(jsonPath("$.status").value("PAID"))
                .andExpect(jsonPath("$.paymentNote").value("Cash desk"));
    }

    @Test
    @DisplayName("POST /api/fines/{id}/pay returns 404 NOT FOUND when fine missing")
    void testPayFine_NotFound() throws Exception {
        when(fineService.payFine(eq(999), any()))
                .thenThrow(new FineNotFoundException("Fine record #999 not found."));

        mockMvc.perform(post("/api/fines/999/pay"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("GET /api/fines/{id} returns 200 OK when fine found")
    void testGetFineById_Found() throws Exception {
        Fine fine = new Fine(1, 101, 1, 20.0, 4, "PENDING", null, null);
        when(fineService.getFineById(1)).thenReturn(Optional.of(fine));

        mockMvc.perform(get("/api/fines/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fineId").value(1))
                .andExpect(jsonPath("$.amount").value(20.0));
    }
}
