package com.booknest.backend.service;

import com.booknest.backend.dto.IssueRequest;
import com.booknest.backend.exception.CirculationNotFoundException;
import com.booknest.backend.exception.CirculationValidationException;
import com.booknest.backend.model.Book;
import com.booknest.backend.model.CirculationRecord;
import com.booknest.backend.model.Member;
import com.booknest.backend.repository.BookRepository;
import com.booknest.backend.repository.CirculationRepository;
import com.booknest.backend.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CirculationServiceTest {

    @Mock
    private CirculationRepository circulationRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private FineService fineService;

    @InjectMocks
    private CirculationService circulationService;

    private Book testBook;
    private Member testMember;

    @BeforeEach
    void setUp() {
        circulationService.setDefaultLoanDays(14);
        testBook = new Book(101, "Clean Code", "Robert C. Martin", 5, 3);
        testMember = new Member(1, "Aarav Sharma", "aarav@college.edu", "9876543210", "CSE", "Student", "Active");
    }

    // =========================================================================
    // 1. Successfully issuing an available book
    // =========================================================================
    @Test
    @DisplayName("Case 1: Should successfully issue an available book to a valid member")
    void testIssueBook_Success() {
        IssueRequest request = new IssueRequest(101, 1, 14);

        when(bookRepository.findById(101)).thenReturn(Optional.of(testBook));
        when(memberRepository.findById(1)).thenReturn(Optional.of(testMember));
        when(bookRepository.decrementAvailableCopies(101)).thenReturn(1);

        CirculationRecord savedRecord = new CirculationRecord(
                1, 101, 1, LocalDate.now(), LocalDate.now().plusDays(14), null, "ISSUED"
        );
        when(circulationRepository.save(any(CirculationRecord.class))).thenReturn(savedRecord);

        CirculationRecord result = circulationService.issueBook(request);

        assertNotNull(result);
        assertEquals(1, result.getIssueId());
        assertEquals(101, result.getBookId());
        assertEquals(1, result.getMemberId());
        assertEquals("ISSUED", result.getStatus());
        assertEquals("Clean Code", result.getBookTitle());
        assertEquals("Aarav Sharma", result.getMemberName());
        verify(bookRepository, times(1)).decrementAvailableCopies(101);
        verify(circulationRepository, times(1)).save(any(CirculationRecord.class));
    }

    // =========================================================================
    // 2. Rejecting an issue when no copies are available
    // =========================================================================
    @Test
    @DisplayName("Case 2: Should reject issue when book has zero available copies")
    void testIssueBook_RejectWhenNoCopiesAvailable() {
        testBook.setAvailableCopies(0);
        IssueRequest request = new IssueRequest(101, 1, 14);

        when(bookRepository.findById(101)).thenReturn(Optional.of(testBook));

        CirculationValidationException ex = assertThrows(
                CirculationValidationException.class,
                () -> circulationService.issueBook(request)
        );

        assertTrue(ex.getMessage().contains("out of stock") || ex.getMessage().contains("No copies available"));
        verify(bookRepository, never()).decrementAvailableCopies(anyInt());
        verify(circulationRepository, never()).save(any());
    }

    // =========================================================================
    // 3. Rejecting an invalid book or member ID
    // =========================================================================
    @Test
    @DisplayName("Case 3a: Should reject issue when book ID is invalid or not found")
    void testIssueBook_RejectInvalidBookId() {
        // Non-positive book ID
        IssueRequest invalidIdRequest = new IssueRequest(-5, 1, 14);
        assertThrows(CirculationValidationException.class, () -> circulationService.issueBook(invalidIdRequest));

        // Non-existent book ID
        IssueRequest notFoundRequest = new IssueRequest(999, 1, 14);
        when(bookRepository.findById(999)).thenReturn(Optional.empty());
        assertThrows(CirculationValidationException.class, () -> circulationService.issueBook(notFoundRequest));

        verify(bookRepository, never()).decrementAvailableCopies(anyInt());
    }

    @Test
    @DisplayName("Case 3b: Should reject issue when member ID is invalid or not found")
    void testIssueBook_RejectInvalidMemberId() {
        // Non-positive member ID
        IssueRequest invalidMemberId = new IssueRequest(101, 0, 14);
        assertThrows(CirculationValidationException.class, () -> circulationService.issueBook(invalidMemberId));

        // Non-existent member ID
        IssueRequest notFoundMember = new IssueRequest(101, 999, 14);
        when(bookRepository.findById(101)).thenReturn(Optional.of(testBook));
        when(memberRepository.findById(999)).thenReturn(Optional.empty());
        assertThrows(CirculationValidationException.class, () -> circulationService.issueBook(notFoundMember));

        verify(bookRepository, never()).decrementAvailableCopies(anyInt());
    }

    // =========================================================================
    // 4. Correctly reducing available copies after issuing
    // =========================================================================
    @Test
    @DisplayName("Case 4: Correctly reduces available copies after issuing")
    void testIssueBook_ReducesAvailableCopies() {
        IssueRequest request = new IssueRequest(101, 1, 14);

        when(bookRepository.findById(101)).thenReturn(Optional.of(testBook));
        when(memberRepository.findById(1)).thenReturn(Optional.of(testMember));
        when(bookRepository.decrementAvailableCopies(101)).thenReturn(1);

        CirculationRecord savedRecord = new CirculationRecord(
                1, 101, 1, LocalDate.now(), LocalDate.now().plusDays(14), null, "ISSUED"
        );
        when(circulationRepository.save(any(CirculationRecord.class))).thenReturn(savedRecord);

        circulationService.issueBook(request);

        // Verify decrement was invoked exactly once with book ID 101
        verify(bookRepository, times(1)).decrementAvailableCopies(101);
    }

    // =========================================================================
    // 5. Successfully returning an issued book
    // =========================================================================
    @Test
    @DisplayName("Case 5: Successfully returning an issued book updates status and records return date")
    void testReturnBook_Success() {
        LocalDate issueDate = LocalDate.now().minusDays(10);
        LocalDate dueDate = issueDate.plusDays(14);
        CirculationRecord activeLoan = new CirculationRecord(
                1, 101, 1, issueDate, dueDate, null, "ISSUED"
        );

        when(circulationRepository.findById(1)).thenReturn(Optional.of(activeLoan));
        when(circulationRepository.markReturned(eq(1), any(LocalDate.class))).thenReturn(1);
        when(bookRepository.incrementAvailableCopies(101)).thenReturn(1);
        when(fineService.calculateOverdueDays(eq(dueDate), any(LocalDate.class))).thenReturn(0);
        when(fineService.calculateFineAmount(eq(dueDate), any(LocalDate.class))).thenReturn(0.0);

        CirculationRecord result = circulationService.returnBook(1, LocalDate.now());

        assertNotNull(result);
        assertEquals("RETURNED", result.getStatus());
        assertEquals(LocalDate.now(), result.getReturnDate());
        assertEquals(0, result.getDaysOverdue());
        assertEquals(0.0, result.getFineAmount());
        verify(circulationRepository, times(1)).markReturned(eq(1), any(LocalDate.class));
    }

    // =========================================================================
    // 6. Correctly increasing available copies after returning
    // =========================================================================
    @Test
    @DisplayName("Case 6: Correctly increases available copies exactly once after returning")
    void testReturnBook_IncrementsAvailableCopies() {
        CirculationRecord activeLoan = new CirculationRecord(
                1, 101, 1, LocalDate.now().minusDays(5), LocalDate.now().plusDays(9), null, "ISSUED"
        );

        when(circulationRepository.findById(1)).thenReturn(Optional.of(activeLoan));
        when(circulationRepository.markReturned(eq(1), any(LocalDate.class))).thenReturn(1);
        when(bookRepository.incrementAvailableCopies(101)).thenReturn(1);

        circulationService.returnBook(1, LocalDate.now());

        verify(bookRepository, times(1)).incrementAvailableCopies(101);
    }

    @Test
    @DisplayName("Rejects a return when the inventory count cannot be increased")
    void testReturnBook_RejectsWhenInventoryCannotBeRestocked() {
        CirculationRecord activeLoan = new CirculationRecord(
                1, 101, 1, LocalDate.now().minusDays(5), LocalDate.now().plusDays(9), null, "ISSUED"
        );
        when(circulationRepository.findById(1)).thenReturn(Optional.of(activeLoan));
        when(circulationRepository.markReturned(eq(1), any(LocalDate.class))).thenReturn(1);
        when(bookRepository.incrementAvailableCopies(101)).thenReturn(0);

        CirculationValidationException exception = assertThrows(
                CirculationValidationException.class,
                () -> circulationService.returnBook(1, LocalDate.now())
        );

        assertTrue(exception.getMessage().contains("total-copy limit"));
        verify(fineService, never()).recordFine(anyInt(), anyInt(), anyInt(), anyDouble());
    }

    // =========================================================================
    // 7. Preventing duplicate returns
    // =========================================================================
    @Test
    @DisplayName("Case 7: Prevents duplicate returns and rejects already returned loans")
    void testReturnBook_PreventDuplicateReturn() {
        CirculationRecord alreadyReturned = new CirculationRecord(
                1, 101, 1, LocalDate.now().minusDays(20), LocalDate.now().minusDays(6), LocalDate.now().minusDays(6), "RETURNED"
        );

        when(circulationRepository.findById(1)).thenReturn(Optional.of(alreadyReturned));

        CirculationValidationException ex = assertThrows(
                CirculationValidationException.class,
                () -> circulationService.returnBook(1, LocalDate.now())
        );

        assertTrue(ex.getMessage().contains("already been returned"));
        verify(circulationRepository, never()).markReturned(anyInt(), any());
        verify(bookRepository, never()).incrementAvailableCopies(anyInt());
    }

    // =========================================================================
    // 8. Calculating zero fine when returned on time
    // =========================================================================
    @Test
    @DisplayName("Case 8: Calculates zero fine when returned on or before due date")
    void testReturnBook_ZeroFineOnTimeReturn() {
        LocalDate issueDate = LocalDate.of(2026, 10, 1);
        LocalDate dueDate = LocalDate.of(2026, 10, 15);
        LocalDate returnDate = LocalDate.of(2026, 10, 14); // 1 day before due date

        CirculationRecord loan = new CirculationRecord(
                1, 101, 1, issueDate, dueDate, null, "ISSUED"
        );

        when(circulationRepository.findById(1)).thenReturn(Optional.of(loan));
        when(circulationRepository.markReturned(1, returnDate)).thenReturn(1);
        when(bookRepository.incrementAvailableCopies(101)).thenReturn(1);
        when(fineService.calculateOverdueDays(dueDate, returnDate)).thenReturn(0);
        when(fineService.calculateFineAmount(dueDate, returnDate)).thenReturn(0.0);

        CirculationRecord result = circulationService.returnBook(1, returnDate);

        assertEquals(0, result.getDaysOverdue());
        assertEquals(0.0, result.getFineAmount());
        assertEquals("NONE", result.getFineStatus());
        verify(fineService, never()).recordFine(anyInt(), anyInt(), anyInt(), anyDouble());
    }

    // =========================================================================
    // 9. Calculating the correct fine for a late return
    // =========================================================================
    @Test
    @DisplayName("Case 9: Calculates correct fine for late return (e.g. 4 days overdue * ₹5 = ₹20)")
    void testReturnBook_CalculatesCorrectFineForLateReturn() {
        LocalDate issueDate = LocalDate.of(2026, 10, 1);
        LocalDate dueDate = LocalDate.of(2026, 10, 15);
        LocalDate returnDate = LocalDate.of(2026, 10, 19); // 4 days late

        CirculationRecord loan = new CirculationRecord(
                1, 101, 1, issueDate, dueDate, null, "ISSUED"
        );

        when(circulationRepository.findById(1)).thenReturn(Optional.of(loan));
        when(circulationRepository.markReturned(1, returnDate)).thenReturn(1);
        when(bookRepository.incrementAvailableCopies(101)).thenReturn(1);
        when(fineService.calculateOverdueDays(dueDate, returnDate)).thenReturn(4);
        when(fineService.calculateFineAmount(dueDate, returnDate)).thenReturn(20.0);

        CirculationRecord result = circulationService.returnBook(1, returnDate);

        assertEquals(4, result.getDaysOverdue());
        assertEquals(20.0, result.getFineAmount());
        assertEquals("PENDING", result.getFineStatus());
        verify(fineService, times(1)).recordFine(1, 1, 4, 20.0);
    }

    // =========================================================================
    // 10. Calculating an outstanding fine for a book overdue and not yet returned
    // =========================================================================
    @Test
    @DisplayName("Case 10: Dynamically calculates outstanding fine for active overdue loan")
    void testGetCirculationById_CalculatesOutstandingFineForActiveOverdue() {
        LocalDate issueDate = LocalDate.now().minusDays(20);
        LocalDate dueDate = LocalDate.now().minusDays(6); // 6 days overdue today

        CirculationRecord loan = new CirculationRecord(
                1, 101, 1, issueDate, dueDate, null, "ISSUED"
        );

        when(circulationRepository.findById(1)).thenReturn(Optional.of(loan));
        when(fineService.calculateOverdueDays(eq(dueDate), any(LocalDate.class))).thenReturn(6);
        when(fineService.calculateFineAmount(eq(dueDate), any(LocalDate.class))).thenReturn(30.0);

        Optional<CirculationRecord> result = circulationService.getCirculationById(1);

        assertTrue(result.isPresent());
        assertEquals(6, result.get().getDaysOverdue());
        assertEquals(30.0, result.get().getFineAmount());
        assertEquals("PENDING", result.get().getFineStatus());
    }
}
