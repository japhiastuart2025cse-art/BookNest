package com.booknest.backend.model;

import java.time.LocalDate;

/**
 * Represents an overdue fine assessment or payment record.
 */
public class Fine {
    private Integer fineId;
    private Integer issueId;
    private Integer memberId;
    private double amount;
    private int daysOverdue;
    private String status; // "PENDING", "PAID"
    private LocalDate paidDate;
    private String paymentNote;

    // Enriched fields for presentation
    private String bookTitle;
    private String memberName;
    private String memberEmail;
    private LocalDate dueDate;
    private LocalDate returnDate;
    private double ratePerDay;

    public Fine() {
    }

    public Fine(Integer fineId, Integer issueId, Integer memberId, double amount,
                int daysOverdue, String status, LocalDate paidDate, String paymentNote) {
        this.fineId = fineId;
        this.issueId = issueId;
        this.memberId = memberId;
        this.amount = amount;
        this.daysOverdue = daysOverdue;
        this.status = status;
        this.paidDate = paidDate;
        this.paymentNote = paymentNote;
    }

    public Integer getFineId() {
        return fineId;
    }

    public void setFineId(Integer fineId) {
        this.fineId = fineId;
    }

    public Integer getIssueId() {
        return issueId;
    }

    public void setIssueId(Integer issueId) {
        this.issueId = issueId;
    }

    public Integer getMemberId() {
        return memberId;
    }

    public void setMemberId(Integer memberId) {
        this.memberId = memberId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public int getDaysOverdue() {
        return daysOverdue;
    }

    public void setDaysOverdue(int daysOverdue) {
        this.daysOverdue = daysOverdue;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDate getPaidDate() {
        return paidDate;
    }

    public void setPaidDate(LocalDate paidDate) {
        this.paidDate = paidDate;
    }

    public String getPaymentNote() {
        return paymentNote;
    }

    public void setPaymentNote(String paymentNote) {
        this.paymentNote = paymentNote;
    }

    public String getBookTitle() {
        return bookTitle;
    }

    public void setBookTitle(String bookTitle) {
        this.bookTitle = bookTitle;
    }

    public String getMemberName() {
        return memberName;
    }

    public void setMemberName(String memberName) {
        this.memberName = memberName;
    }

    public String getMemberEmail() {
        return memberEmail;
    }

    public void setMemberEmail(String memberEmail) {
        this.memberEmail = memberEmail;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public LocalDate getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(LocalDate returnDate) {
        this.returnDate = returnDate;
    }

    public double getRatePerDay() {
        return ratePerDay;
    }

    public void setRatePerDay(double ratePerDay) {
        this.ratePerDay = ratePerDay;
    }

    @Override
    public String toString() {
        return "Fine{" +
                "fineId=" + fineId +
                ", issueId=" + issueId +
                ", memberId=" + memberId +
                ", amount=" + amount +
                ", daysOverdue=" + daysOverdue +
                ", status='" + status + '\'' +
                ", paidDate=" + paidDate +
                '}';
    }
}
