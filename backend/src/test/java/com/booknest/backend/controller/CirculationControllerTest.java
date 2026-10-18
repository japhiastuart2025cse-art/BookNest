package com.booknest.backend.controller;

import com.booknest.backend.dto.IssueRequest;
import com.booknest.backend.exception.CirculationNotFoundException;
import com.booknest.backend.exception.CirculationValidationException;
import com.booknest.backend.exception.GlobalExceptionHandler;
import com.booknest.backend.model.CirculationRecord;
import com.booknest.backend.service.CirculationService;
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
class CirculationControllerTest {

    private MockMvc mockMvc;

    @Mock
    private CirculationService circulationService;

    @InjectMocks
    private CirculationController circulationController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(circulationController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("POST /api/circulation/issue returns 201 CREATED for valid issue")
    void testIssueBook_Success() throws Exception {
        CirculationRecord record = new CirculationRecord(
                1, 101, 1, LocalDate.now(), LocalDate.now().plusDays(14), null, "ISSUED"
        );
        record.setBookTitle("Clean Code");
        record.setMemberName("Aarav Sharma");

        when(circulationService.issueBook(any(IssueRequest.class))).thenReturn(record);

        String json = """
                {
                    "bookId": 101,
                    "memberId": 1,
                    "loanDays": 14
                }
                """;

        mockMvc.perform(post("/api/circulation/issue")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.issueId").value(1))
                .andExpect(jsonPath("$.bookId").value(101))
                .andExpect(jsonPath("$.memberId").value(1))
                .andExpect(jsonPath("$.status").value("ISSUED"))
                .andExpect(jsonPath("$.bookTitle").value("Clean Code"))
                .andExpect(jsonPath("$.memberName").value("Aarav Sharma"));
    }

    @Test
    @DisplayName("POST /api/circulation/issue returns 400 BAD REQUEST when validation fails")
    void testIssueBook_ValidationFailure() throws Exception {
        when(circulationService.issueBook(any(IssueRequest.class)))
                .thenThrow(new CirculationValidationException("Book is currently out of stock."));

        String json = """
                {
                    "bookId": 101,
                    "memberId": 1
                }
                """;

        mockMvc.perform(post("/api/circulation/issue")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Book is currently out of stock."));
    }

    @Test
    @DisplayName("POST /api/circulation/{id}/return returns 200 OK when book returned")
    void testReturnBook_Success() throws Exception {
        CirculationRecord returned = new CirculationRecord(
                1, 101, 1, LocalDate.now().minusDays(10), LocalDate.now().plusDays(4), LocalDate.now(), "RETURNED"
        );
        returned.setDaysOverdue(0);
        returned.setFineAmount(0.0);

        when(circulationService.returnBook(eq(1), any(LocalDate.class))).thenReturn(returned);

        mockMvc.perform(post("/api/circulation/1/return")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.issueId").value(1))
                .andExpect(jsonPath("$.status").value("RETURNED"))
                .andExpect(jsonPath("$.daysOverdue").value(0))
                .andExpect(jsonPath("$.fineAmount").value(0.0));
    }

    @Test
    @DisplayName("POST /api/circulation/{id}/return returns 404 NOT FOUND when loan does not exist")
    void testReturnBook_NotFound() throws Exception {
        when(circulationService.returnBook(eq(999), any(LocalDate.class)))
                .thenThrow(new CirculationNotFoundException("Circulation record #999 not found."));

        mockMvc.perform(post("/api/circulation/999/return"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Circulation record #999 not found."));
    }

    @Test
    @DisplayName("GET /api/circulation returns 200 OK with list of all circulation records")
    void testGetAllCirculation() throws Exception {
        CirculationRecord r1 = new CirculationRecord(1, 101, 1, LocalDate.now(), LocalDate.now().plusDays(14), null, "ISSUED");
        CirculationRecord r2 = new CirculationRecord(2, 102, 2, LocalDate.now().minusDays(20), LocalDate.now().minusDays(6), LocalDate.now(), "RETURNED");

        when(circulationService.getAllCirculation()).thenReturn(List.of(r1, r2));

        mockMvc.perform(get("/api/circulation"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].issueId").value(1))
                .andExpect(jsonPath("$[1].issueId").value(2));
    }

    @Test
    @DisplayName("GET /api/circulation/{id} returns 200 OK when found")
    void testGetCirculationById_Found() throws Exception {
        CirculationRecord record = new CirculationRecord(1, 101, 1, LocalDate.now(), LocalDate.now().plusDays(14), null, "ISSUED");
        when(circulationService.getCirculationById(1)).thenReturn(Optional.of(record));

        mockMvc.perform(get("/api/circulation/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.issueId").value(1));
    }
}
