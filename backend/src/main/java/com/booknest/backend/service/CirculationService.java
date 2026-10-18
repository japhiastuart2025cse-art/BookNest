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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class CirculationService {

    private final CirculationRepository circulationRepository;
    private final BookRepository bookRepository;
    private final MemberRepository memberRepository;
    private final FineService fineService;

    @Value("${booknest.circulation.default-loan-days:14}")
    private int defaultLoanDays;

    public CirculationService(CirculationRepository circulationRepository,
                              BookRepository bookRepository,
                              MemberRepository memberRepository,
                              FineService fineService) {
        this.circulationRepository = circulationRepository;
        this.bookRepository = bookRepository;
        this.memberRepository = memberRepository;
        this.fineService = fineService;
    }

    public int getDefaultLoanDays() {
        return defaultLoanDays;
    }

    public void setDefaultLoanDays(int defaultLoanDays) {
        this.defaultLoanDays = defaultLoanDays;
    }

    /**
     * Issue a book to a registered member.
     * Transactional: ensures book copy decrement and circulation creation happen atomically.
     * Prevents race conditions and guarantees referential consistency.
     */
    @Transactional
    public CirculationRecord issueBook(IssueRequest request) {
        if (request == null) {
            throw new CirculationValidationException("Issue request body must not be null.");
        }
        if (request.getBookId() == null || request.getBookId() <= 0) {
            throw new CirculationValidationException("A valid positive Book ID is required.");
        }
        if (request.getMemberId() == null || request.getMemberId() <= 0) {
            throw new CirculationValidationException("A valid positive Member ID is required.");
        }

        // Validate Book existence and availability
        Book book = bookRepository.findById(request.getBookId())
                .orElseThrow(() -> new CirculationValidationException("Book with ID " + request.getBookId() + " does not exist."));

        if (book.getAvailableCopies() == null || book.getAvailableCopies() <= 0) {
            throw new CirculationValidationException("Book '" + book.getTitle() + "' (ID #" + book.getId() + ") is currently out of stock. No copies available.");
        }

        // Validate Member existence and eligibility
        Member member = memberRepository.findById(request.getMemberId())
                .orElseThrow(() -> new CirculationValidationException("Member with ID " + request.getMemberId() + " does not exist."));

        if (member.getStatus() != null && member.getStatus().toLowerCase().contains("suspended")) {
            throw new CirculationValidationException("Member '" + member.getName() + "' account is suspended and cannot borrow books.");
        }

        // Atomically decrement available copy count in MySQL to eliminate race conditions
        int updatedRows = bookRepository.decrementAvailableCopies(book.getId());
        if (updatedRows == 0) {
            throw new CirculationValidationException("Failed to reserve book copy. Insufficient available copies.");
        }

        int loanDays = (request.getLoanDays() != null && request.getLoanDays() > 0)
                ? request.getLoanDays()
                : defaultLoanDays;

        LocalDate today = LocalDate.now();
        LocalDate dueDate = today.plusDays(loanDays);

        CirculationRecord record = new CirculationRecord();
        record.setBookId(book.getId());
        record.setMemberId(member.getId());
        record.setIssueDate(today);
        record.setDueDate(dueDate);
        record.setStatus("ISSUED");

        CirculationRecord saved = circulationRepository.save(record);
        saved.setBookTitle(book.getTitle());
        saved.setBookAuthor(book.getAuthor());
        saved.setMemberName(member.getName());
        saved.setMemberEmail(member.getEmail());
        saved.setMemberDepartment(member.getDepartment());
        saved.setDaysOverdue(0);
        saved.setFineAmount(0.0);
        saved.setFineStatus("NONE");

        return saved;
    }

    /**
     * Process return / check-in of an issued book.
     * Transactional: marks loan RETURNED, restocks available copies exactly once,
     * and calculates/records overdue fine if returned late.
     */
    @Transactional
    public CirculationRecord returnBook(int issueId, LocalDate returnDate) {
        if (issueId <= 0) {
            throw new CirculationValidationException("Issue ID must be greater than zero.");
        }

        CirculationRecord record = circulationRepository.findById(issueId)
                .orElseThrow(() -> new CirculationNotFoundException("Circulation record #" + issueId + " not found."));

        if ("RETURNED".equalsIgnoreCase(record.getStatus())) {
            throw new CirculationValidationException("Book from circulation record #" + issueId + " has already been returned.");
        }

        LocalDate actualReturnDate = (returnDate != null) ? returnDate : LocalDate.now();

        // Mark returned in circulation table
        int rowsUpdated = circulationRepository.markReturned(issueId, actualReturnDate);
        if (rowsUpdated == 0) {
            throw new CirculationValidationException("Could not update circulation record #" + issueId + ".");
        }

        // Restock available copies in books inventory (guaranteed exactly once)
        int copiesRestocked = bookRepository.incrementAvailableCopies(record.getBookId());
        if (copiesRestocked == 0) {
            throw new CirculationValidationException(
                "Could not return this book because its available-copy count is already at the total-copy limit."
            );
        }

        // Calculate overdue days and late fine
        int overdueDays = fineService.calculateOverdueDays(record.getDueDate(), actualReturnDate);
        double fineAmount = fineService.calculateFineAmount(record.getDueDate(), actualReturnDate);

        if (overdueDays > 0) {
            fineService.recordFine(issueId, record.getMemberId(), overdueDays, fineAmount);
            record.setFineStatus("PENDING");
        } else {
            record.setFineStatus("NONE");
        }

        record.setStatus("RETURNED");
        record.setReturnDate(actualReturnDate);
        record.setDaysOverdue(overdueDays);
        record.setFineAmount(fineAmount);

        return record;
    }

    /**
     * List all circulation records with dynamic overdue and fine enrichment.
     */
    public List<CirculationRecord> getAllCirculation() {
        List<CirculationRecord> records = circulationRepository.findAll();
        enrichCirculationRecords(records);
        return records;
    }

    /**
     * List only active loans (not yet returned).
     */
    public List<CirculationRecord> getActiveLoans() {
        List<CirculationRecord> records = circulationRepository.findActiveLoans();
        enrichCirculationRecords(records);
        return records;
    }

    /**
     * List only overdue active loans.
     */
    public List<CirculationRecord> getOverdueLoans() {
        List<CirculationRecord> records = circulationRepository.findOverdueLoans(LocalDate.now());
        enrichCirculationRecords(records);
        return records;
    }

    public Optional<CirculationRecord> getCirculationById(int issueId) {
        Optional<CirculationRecord> recordOpt = circulationRepository.findById(issueId);
        recordOpt.ifPresent(this::enrichSingleRecord);
        return recordOpt;
    }

    private void enrichCirculationRecords(List<CirculationRecord> records) {
        for (CirculationRecord r : records) {
            enrichSingleRecord(r);
        }
    }

    private void enrichSingleRecord(CirculationRecord r) {
        LocalDate comparisonDate = (r.getReturnDate() != null) ? r.getReturnDate() : LocalDate.now();
        int overdueDays = fineService.calculateOverdueDays(r.getDueDate(), comparisonDate);
        double fineAmount = fineService.calculateFineAmount(r.getDueDate(), comparisonDate);

        r.setDaysOverdue(overdueDays);
        if ("RETURNED".equalsIgnoreCase(r.getStatus())) {
            if (overdueDays > 0) {
                if (r.getFineStatus() == null || "NONE".equals(r.getFineStatus())) {
                    r.setFineStatus("PENDING");
                }
            } else {
                r.setFineStatus("NONE");
                r.setFineAmount(0.0);
            }
        } else {
            // Still issued
            if (overdueDays > 0) {
                r.setFineAmount(fineAmount);
                if (r.getFineStatus() == null || "NONE".equals(r.getFineStatus())) {
                    r.setFineStatus("PENDING");
                }
            } else {
                r.setFineAmount(0.0);
                r.setFineStatus("NONE");
            }
        }
    }
}
