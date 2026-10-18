package com.booknest.backend.controller;

import com.booknest.backend.dto.IssueRequest;
import com.booknest.backend.exception.CirculationValidationException;
import com.booknest.backend.model.CirculationRecord;
import com.booknest.backend.model.Fine;
import com.booknest.backend.service.CirculationService;
import com.booknest.backend.service.FineService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class CirculationIntegrationTest {

    private static final int TEST_BOOK_ID = 8881;
    private static final int TEST_MEMBER_ID = 881;

    @Autowired
    private CirculationService circulationService;

    @Autowired
    private FineService fineService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        tearDown(); // Clean slate

        // Insert test book in books table
        jdbcTemplate.update(
                "INSERT INTO books (book_id, title, author, total_copies, available_copies) VALUES (?, ?, ?, ?, ?)",
                TEST_BOOK_ID, "Spring Boot in Action", "Craig Walls", 5, 5
        );

        // Insert test member in members table
        jdbcTemplate.update(
                "INSERT INTO members (member_id, name, email, phone, department, role, status) VALUES (?, ?, ?, ?, ?, ?, ?)",
                TEST_MEMBER_ID, "Integration Test User", "integ.user@college.edu", "9999999999", "CSE", "Student", "Active"
        );
    }

    @AfterEach
    void tearDown() {
        // Clean up fines, circulation, members, books created during tests
        jdbcTemplate.update("DELETE FROM fines WHERE member_id = ?", TEST_MEMBER_ID);
        jdbcTemplate.update("DELETE FROM circulation WHERE member_id = ? OR book_id = ?", TEST_MEMBER_ID, TEST_BOOK_ID);
        jdbcTemplate.update("DELETE FROM members WHERE member_id = ?", TEST_MEMBER_ID);
        jdbcTemplate.update("DELETE FROM books WHERE book_id = ?", TEST_BOOK_ID);
    }

    @Test
    @DisplayName("Integration: Issue book reduces available copies in MySQL and creates circulation record")
    void testIssueBook_ReducesCopiesInMySQL() {
        IssueRequest request = new IssueRequest(TEST_BOOK_ID, TEST_MEMBER_ID, 14);

        CirculationRecord record = circulationService.issueBook(request);
        assertNotNull(record.getIssueId());
        assertEquals("ISSUED", record.getStatus());

        // Verify available copies reduced from 5 to 4 in MySQL
        Integer availableCopies = jdbcTemplate.queryForObject(
                "SELECT available_copies FROM books WHERE book_id = ?",
                Integer.class,
                TEST_BOOK_ID
        );
        assertEquals(4, availableCopies);

        // Verify row exists in circulation table
        Integer issueCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM circulation WHERE issue_id = ? AND status = 'ISSUED'",
                Integer.class,
                record.getIssueId()
        );
        assertEquals(1, issueCount);
    }

    @Test
    @DisplayName("Integration: Reject issue when available copies is 0 and preserve inventory count")
    void testIssueBook_RejectWhenNoCopiesAvailable() {
        // Drain available copies to 0
        jdbcTemplate.update("UPDATE books SET available_copies = 0 WHERE book_id = ?", TEST_BOOK_ID);

        IssueRequest request = new IssueRequest(TEST_BOOK_ID, TEST_MEMBER_ID, 14);

        assertThrows(CirculationValidationException.class, () -> circulationService.issueBook(request));

        // Confirm available copies remains 0
        Integer availableCopies = jdbcTemplate.queryForObject(
                "SELECT available_copies FROM books WHERE book_id = ?",
                Integer.class,
                TEST_BOOK_ID
        );
        assertEquals(0, availableCopies);

        // Confirm no circulation record was created
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM circulation WHERE book_id = ?",
                Integer.class,
                TEST_BOOK_ID
        );
        assertEquals(0, count);
    }

    @Test
    @DisplayName("Integration: Return book increases available copies exactly once in MySQL and charges 0 fine when on time")
    void testReturnBook_IncreasesCopiesAndZeroFineOnTime() {
        // Issue book first (available copies: 5 -> 4)
        IssueRequest request = new IssueRequest(TEST_BOOK_ID, TEST_MEMBER_ID, 14);
        CirculationRecord issued = circulationService.issueBook(request);

        // Return book on time
        CirculationRecord returned = circulationService.returnBook(issued.getIssueId(), LocalDate.now());
        assertEquals("RETURNED", returned.getStatus());
        assertEquals(0, returned.getDaysOverdue());
        assertEquals(0.0, returned.getFineAmount());

        // Verify copies incremented back to 5 in MySQL
        Integer availableCopies = jdbcTemplate.queryForObject(
                "SELECT available_copies FROM books WHERE book_id = ?",
                Integer.class,
                TEST_BOOK_ID
        );
        assertEquals(5, availableCopies);

        // Verify no overdue fine created
        Integer fineCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM fines WHERE issue_id = ?",
                Integer.class,
                issued.getIssueId()
        );
        assertEquals(0, fineCount);
    }

    @Test
    @DisplayName("Integration: Duplicate return rejected and does not increment copies again")
    void testReturnBook_PreventDuplicateReturn() {
        IssueRequest request = new IssueRequest(TEST_BOOK_ID, TEST_MEMBER_ID, 14);
        CirculationRecord issued = circulationService.issueBook(request);

        // First return succeeds
        circulationService.returnBook(issued.getIssueId(), LocalDate.now());

        // Second return must throw exception
        assertThrows(CirculationValidationException.class, () ->
                circulationService.returnBook(issued.getIssueId(), LocalDate.now())
        );

        // Verify copies remain at 5 and was NOT incremented to 6
        Integer availableCopies = jdbcTemplate.queryForObject(
                "SELECT available_copies FROM books WHERE book_id = ?",
                Integer.class,
                TEST_BOOK_ID
        );
        assertEquals(5, availableCopies);
    }

    @Test
    @DisplayName("Integration: Late return records correct fine in MySQL and payment updates status to PAID")
    void testLateReturn_RecordsFineAndPaySucceeds() {
        // Manually insert an overdue loan (due 6 days ago)
        LocalDate issueDate = LocalDate.now().minusDays(20);
        LocalDate dueDate = LocalDate.now().minusDays(6);

        jdbcTemplate.update(
                "INSERT INTO circulation (book_id, member_id, issue_date, due_date, status) VALUES (?, ?, ?, ?, 'ISSUED')",
                TEST_BOOK_ID, TEST_MEMBER_ID, issueDate, dueDate
        );
        Integer issueId = jdbcTemplate.queryForObject(
                "SELECT issue_id FROM circulation WHERE book_id = ? AND member_id = ? AND status = 'ISSUED'",
                Integer.class,
                TEST_BOOK_ID, TEST_MEMBER_ID
        );

        // Available copies: decrement to simulate issue
        jdbcTemplate.update("UPDATE books SET available_copies = 4 WHERE book_id = ?", TEST_BOOK_ID);

        // Return book today (6 days overdue)
        CirculationRecord returned = circulationService.returnBook(issueId, LocalDate.now());
        assertEquals("RETURNED", returned.getStatus());
        assertEquals(6, returned.getDaysOverdue());
        assertEquals(30.0, returned.getFineAmount()); // 6 days * ₹5.00 = ₹30.00

        // Verify fine recorded in MySQL fines table
        Double recordedAmount = jdbcTemplate.queryForObject(
                "SELECT amount FROM fines WHERE issue_id = ?",
                Double.class,
                issueId
        );
        assertEquals(30.0, recordedAmount);

        String fineStatus = jdbcTemplate.queryForObject(
                "SELECT status FROM fines WHERE issue_id = ?",
                String.class,
                issueId
        );
        assertEquals("PENDING", fineStatus);

        Integer fineId = jdbcTemplate.queryForObject(
                "SELECT fine_id FROM fines WHERE issue_id = ?",
                Integer.class,
                issueId
        );

        // Pay the fine
        Fine paidFine = fineService.payFine(fineId, "Paid in cash at desk");
        assertEquals("PAID", paidFine.getStatus());
        assertEquals(LocalDate.now(), paidFine.getPaidDate());

        // Verify MySQL fines table updated to PAID
        String updatedFineStatus = jdbcTemplate.queryForObject(
                "SELECT status FROM fines WHERE fine_id = ?",
                String.class,
                fineId
        );
        assertEquals("PAID", updatedFineStatus);
    }

    @Test
    @DisplayName("Case 11 Integration: Failed transaction does not leave circulation records and copy counts inconsistent")
    void testTransactionRollback_EnsuresCopyCountAndCirculationConsistentOnFailure() {
        // Record initial available copies
        Integer initialCopies = jdbcTemplate.queryForObject(
                "SELECT available_copies FROM books WHERE book_id = ?",
                Integer.class,
                TEST_BOOK_ID
        );
        assertEquals(5, initialCopies);

        // Attempt issue with non-existent member (ID 99999) which fails validation
        IssueRequest badRequest = new IssueRequest(TEST_BOOK_ID, 99999, 14);

        assertThrows(CirculationValidationException.class, () ->
                circulationService.issueBook(badRequest)
        );

        // Verify available copies remained untouched in MySQL
        Integer copiesAfterFailure = jdbcTemplate.queryForObject(
                "SELECT available_copies FROM books WHERE book_id = ?",
                Integer.class,
                TEST_BOOK_ID
        );
        assertEquals(initialCopies, copiesAfterFailure);

        // Verify no orphan circulation record was inserted into MySQL
        Integer orphanRecords = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM circulation WHERE book_id = ? AND member_id = 99999",
                Integer.class,
                TEST_BOOK_ID
        );
        assertEquals(0, orphanRecords);
    }
}

