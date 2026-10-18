package com.booknest.backend.service;

import com.booknest.backend.dto.FinesSummaryDTO;
import com.booknest.backend.exception.CirculationValidationException;
import com.booknest.backend.exception.FineNotFoundException;
import com.booknest.backend.model.Fine;
import com.booknest.backend.repository.CirculationRepository;
import com.booknest.backend.repository.FineRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FineServiceTest {

    @Mock
    private FineRepository fineRepository;

    @Mock
    private CirculationRepository circulationRepository;

    @InjectMocks
    private FineService fineService;

    @BeforeEach
    void setUp() {
        fineService.setFineRatePerDay(5.00);
    }

    @Test
    @DisplayName("Should return 0 overdue days and ₹0.00 fine when returned on or before due date")
    void testCalculateFine_OnTime() {
        LocalDate dueDate = LocalDate.of(2026, 10, 15);
        LocalDate returnDate = LocalDate.of(2026, 10, 15);

        int days = fineService.calculateOverdueDays(dueDate, returnDate);
        double fine = fineService.calculateFineAmount(dueDate, returnDate);

        assertEquals(0, days);
        assertEquals(0.0, fine);

        // Also test returned before due date
        LocalDate earlyReturn = LocalDate.of(2026, 10, 10);
        assertEquals(0, fineService.calculateOverdueDays(dueDate, earlyReturn));
        assertEquals(0.0, fineService.calculateFineAmount(dueDate, earlyReturn));
    }

    @Test
    @DisplayName("Should correctly calculate overdue days and fine for late return (e.g. 7 days * ₹5 = ₹35)")
    void testCalculateFine_LateReturn() {
        LocalDate dueDate = LocalDate.of(2026, 10, 10);
        LocalDate returnDate = LocalDate.of(2026, 10, 17); // 7 days late

        int days = fineService.calculateOverdueDays(dueDate, returnDate);
        double fine = fineService.calculateFineAmount(dueDate, returnDate);

        assertEquals(7, days);
        assertEquals(35.0, fine);
    }

    @Test
    @DisplayName("Fine rate should be configurable and easy to adjust in one place")
    void testConfigurableFineRate() {
        fineService.setFineRatePerDay(10.00); // adjusted to ₹10 / day
        LocalDate dueDate = LocalDate.of(2026, 10, 10);
        LocalDate returnDate = LocalDate.of(2026, 10, 13); // 3 days late

        double fine = fineService.calculateFineAmount(dueDate, returnDate);

        assertEquals(30.0, fine); // 3 * 10
    }

    @Test
    @DisplayName("Should record new fine when loan is overdue and not yet recorded")
    void testRecordFine_New() {
        when(fineRepository.findByIssueId(1)).thenReturn(Optional.empty());
        Fine savedFine = new Fine(10, 1, 100, 25.0, 5, "PENDING", null, null);
        when(fineRepository.save(any(Fine.class))).thenReturn(savedFine);

        Fine result = fineService.recordFine(1, 100, 5, 25.0);

        assertNotNull(result);
        assertEquals("PENDING", result.getStatus());
        assertEquals(25.0, result.getAmount());
        assertEquals(5, result.getDaysOverdue());
        verify(fineRepository, times(1)).save(any(Fine.class));
    }

    @Test
    @DisplayName("Should successfully pay a pending fine and update database record")
    void testPayFine_Success() {
        Fine pendingFine = new Fine(10, 1, 100, 25.0, 5, "PENDING", null, null);
        when(fineRepository.findById(10)).thenReturn(Optional.of(pendingFine));
        when(fineRepository.markPaid(10, LocalDate.now(), "Cash desk")).thenReturn(1);

        Fine paid = fineService.payFine(10, "Cash desk");

        assertNotNull(paid);
        assertEquals("PAID", paid.getStatus());
        assertEquals(LocalDate.now(), paid.getPaidDate());
        assertEquals("Cash desk", paid.getPaymentNote());
        verify(fineRepository, times(1)).markPaid(10, LocalDate.now(), "Cash desk");
    }

    @Test
    @DisplayName("Should reject payment for already paid fine")
    void testPayFine_AlreadyPaid() {
        Fine paidFine = new Fine(10, 1, 100, 25.0, 5, "PAID", LocalDate.now(), "Already settled");
        when(fineRepository.findById(10)).thenReturn(Optional.of(paidFine));

        CirculationValidationException ex = assertThrows(
                CirculationValidationException.class,
                () -> fineService.payFine(10, "Cash desk")
        );

        assertTrue(ex.getMessage().contains("already been paid"));
    }

    @Test
    @DisplayName("Should throw FineNotFoundException when paying non-existent fine")
    void testPayFine_NotFound() {
        when(fineRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(FineNotFoundException.class, () -> fineService.payFine(999, "Note"));
    }

    @Test
    @DisplayName("Should calculate accurate summary metrics for fines ledger")
    void testGetFinesSummary() {
        Fine f1 = new Fine(1, 101, 1, 20.0, 4, "PENDING", null, null);
        Fine f2 = new Fine(2, 102, 2, 40.0, 8, "PAID", LocalDate.now(), "Online");
        Fine f3 = new Fine(3, 103, 1, 15.0, 3, "PENDING", null, null);

        when(fineRepository.findAll()).thenReturn(List.of(f1, f2, f3));
        when(circulationRepository.findOverdueLoans(any(LocalDate.class))).thenReturn(Collections.emptyList());

        FinesSummaryDTO summary = fineService.getFinesSummary();

        assertEquals(35.0, summary.getPendingTotal()); // 20 + 15
        assertEquals(40.0, summary.getTotalSettled());
        assertEquals(1, summary.getDefaultersCount()); // Member 1 has pending fines
        assertEquals(5.00, summary.getDailyRate());
    }
}
