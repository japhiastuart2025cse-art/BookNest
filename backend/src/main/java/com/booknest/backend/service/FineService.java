package com.booknest.backend.service;

import com.booknest.backend.dto.FinesSummaryDTO;
import com.booknest.backend.exception.CirculationValidationException;
import com.booknest.backend.exception.FineNotFoundException;
import com.booknest.backend.model.CirculationRecord;
import com.booknest.backend.model.Fine;
import com.booknest.backend.repository.CirculationRepository;
import com.booknest.backend.repository.FineRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class FineService {

    private final FineRepository fineRepository;
    private final CirculationRepository circulationRepository;

    @Value("${booknest.circulation.fine-rate-per-day:5.00}")
    private double fineRatePerDay;

    public FineService(FineRepository fineRepository, CirculationRepository circulationRepository) {
        this.fineRepository = fineRepository;
        this.circulationRepository = circulationRepository;
    }

    public double getFineRatePerDay() {
        return fineRatePerDay;
    }

    public void setFineRatePerDay(double fineRatePerDay) {
        this.fineRatePerDay = fineRatePerDay;
    }

    /**
     * Calculate overdue days between due date and target date.
     * Returns 0 if returned on or before due date (no negative days).
     */
    public int calculateOverdueDays(LocalDate dueDate, LocalDate targetDate) {
        if (dueDate == null || targetDate == null) {
            return 0;
        }
        long days = ChronoUnit.DAYS.between(dueDate, targetDate);
        return days > 0 ? (int) days : 0;
    }

    /**
     * Calculate overdue fine based on due date, return date/today, and fine rate.
     */
    public double calculateFineAmount(LocalDate dueDate, LocalDate targetDate) {
        int overdueDays = calculateOverdueDays(dueDate, targetDate);
        return overdueDays * fineRatePerDay;
    }

    /**
     * Get all fines from the ledger.
     * Also integrates currently active overdue loans that have calculated outstanding dues.
     */
    public List<Fine> getAllFines() {
        List<Fine> persistedFines = fineRepository.findAll();
        Set<Integer> handledIssueIds = new HashSet<>();
        for (Fine f : persistedFines) {
            f.setRatePerDay(fineRatePerDay);
            handledIssueIds.add(f.getIssueId());
        }

        // Check active overdue loans not yet persisted in fines table
        List<CirculationRecord> activeOverdue = circulationRepository.findOverdueLoans(LocalDate.now());
        List<Fine> fullLedger = new ArrayList<>(persistedFines);

        for (CirculationRecord loan : activeOverdue) {
            if (!handledIssueIds.contains(loan.getIssueId())) {
                int overdueDays = calculateOverdueDays(loan.getDueDate(), LocalDate.now());
                if (overdueDays > 0) {
                    Fine activeFine = new Fine();
                    activeFine.setFineId(-loan.getIssueId()); // Temporary negative ID to denote unpersisted outstanding
                    activeFine.setIssueId(loan.getIssueId());
                    activeFine.setMemberId(loan.getMemberId());
                    activeFine.setAmount(overdueDays * fineRatePerDay);
                    activeFine.setDaysOverdue(overdueDays);
                    activeFine.setStatus("PENDING");
                    activeFine.setBookTitle(loan.getBookTitle());
                    activeFine.setMemberName(loan.getMemberName());
                    activeFine.setMemberEmail(loan.getMemberEmail());
                    activeFine.setDueDate(loan.getDueDate());
                    activeFine.setRatePerDay(fineRatePerDay);
                    fullLedger.add(activeFine);
                }
            }
        }

        return fullLedger;
    }

    public Optional<Fine> getFineById(int fineId) {
        Optional<Fine> fine = fineRepository.findById(fineId);
        fine.ifPresent(f -> f.setRatePerDay(fineRatePerDay));
        return fine;
    }

    public Optional<Fine> getFineByIssueId(int issueId) {
        Optional<Fine> fine = fineRepository.findByIssueId(issueId);
        fine.ifPresent(f -> f.setRatePerDay(fineRatePerDay));
        return fine;
    }

    /**
     * Records or updates a fine for an overdue book issue.
     */
    @Transactional
    public Fine recordFine(int issueId, int memberId, int daysOverdue, double amount) {
        if (daysOverdue <= 0 || amount <= 0) {
            return null;
        }

        Optional<Fine> existing = fineRepository.findByIssueId(issueId);
        if (existing.isPresent()) {
            Fine fine = existing.get();
            if (!"PAID".equalsIgnoreCase(fine.getStatus())) {
                fine.setDaysOverdue(daysOverdue);
                fine.setAmount(amount);
                fineRepository.update(fine);
            }
            return fine;
        } else {
            Fine fine = new Fine();
            fine.setIssueId(issueId);
            fine.setMemberId(memberId);
            fine.setDaysOverdue(daysOverdue);
            fine.setAmount(amount);
            fine.setStatus("PENDING");
            return fineRepository.save(fine);
        }
    }

    /**
     * Collects and records payment for a fine.
     * Strictly verifies database existence and marks status as PAID.
     */
    @Transactional
    public Fine payFine(int fineId, String paymentNote) {
        // If fineId is negative, it's an outstanding active overdue fine that must be persisted first
        if (fineId < 0) {
            int issueId = -fineId;
            CirculationRecord loan = circulationRepository.findById(issueId)
                    .orElseThrow(() -> new CirculationValidationException("Loan record not found for issue ID: " + issueId));

            int overdueDays = calculateOverdueDays(loan.getDueDate(), LocalDate.now());
            double amount = overdueDays * fineRatePerDay;

            Fine newFine = new Fine();
            newFine.setIssueId(issueId);
            newFine.setMemberId(loan.getMemberId());
            newFine.setDaysOverdue(overdueDays);
            newFine.setAmount(amount);
            newFine.setStatus("PAID");
            newFine.setPaidDate(LocalDate.now());
            newFine.setPaymentNote(paymentNote != null && !paymentNote.isBlank() ? paymentNote.trim() : "Paid at circulation desk");

            Fine saved = fineRepository.save(newFine);
            saved.setBookTitle(loan.getBookTitle());
            saved.setMemberName(loan.getMemberName());
            saved.setRatePerDay(fineRatePerDay);
            return saved;
        }

        Fine fine = fineRepository.findById(fineId)
                .orElseThrow(() -> new FineNotFoundException("Fine record #" + fineId + " not found."));

        if ("PAID".equalsIgnoreCase(fine.getStatus())) {
            throw new CirculationValidationException("Fine #" + fineId + " has already been paid.");
        }

        String note = (paymentNote != null && !paymentNote.isBlank()) ? paymentNote.trim() : "Paid at circulation desk";
        fineRepository.markPaid(fineId, LocalDate.now(), note);

        fine.setStatus("PAID");
        fine.setPaidDate(LocalDate.now());
        fine.setPaymentNote(note);
        fine.setRatePerDay(fineRatePerDay);
        return fine;
    }

    /**
     * Compute summary metrics for fines ledger.
     */
    public FinesSummaryDTO getFinesSummary() {
        List<Fine> allFines = getAllFines();
        double pendingTotal = 0.0;
        double totalSettled = 0.0;
        Set<Integer> defaulterMemberIds = new HashSet<>();

        for (Fine f : allFines) {
            if ("PAID".equalsIgnoreCase(f.getStatus())) {
                totalSettled += f.getAmount();
            } else {
                pendingTotal += f.getAmount();
                defaulterMemberIds.add(f.getMemberId());
            }
        }

        return new FinesSummaryDTO(
                pendingTotal,
                fineRatePerDay,
                0, // Grace period
                defaulterMemberIds.size(),
                totalSettled
        );
    }
}
