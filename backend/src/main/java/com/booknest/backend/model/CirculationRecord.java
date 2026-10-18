package com.booknest.backend.model;

import java.time.LocalDate;

/**
 * Represents a book circulation / loan transaction.
 * Contains both database persistence fields and enriched view fields
 * for frictionless display on the React frontend.
 */
public class CirculationRecord {
    private Integer issueId;
    private Integer bookId;
    private Integer memberId;
    private LocalDate issueDate;
    private LocalDate dueDate;
    private LocalDate returnDate;
    private String status; // "ISSUED", "RETURNED"

    // Enriched display fields
    private String bookTitle;
    private String bookAuthor;
    private String memberName;
    private String memberEmail;
    private String memberDepartment;
    private int daysOverdue;
    private double fineAmount;
    private String fineStatus; // "NONE", "PENDING", "PAID"
    private Integer fineId;

    public CirculationRecord() {
    }

    public CirculationRecord(Integer issueId, Integer bookId, Integer memberId,
                             LocalDate issueDate, LocalDate dueDate, LocalDate returnDate, String status) {
        this.issueId = issueId;
        this.bookId = bookId;
        this.memberId = memberId;
        this.issueDate = issueDate;
        this.dueDate = dueDate;
        this.returnDate = returnDate;
        this.status = status;
    }

    public Integer getIssueId() {
        return issueId;
    }

    public void setIssueId(Integer issueId) {
        this.issueId = issueId;
    }

    public Integer getBookId() {
        return bookId;
    }

    public void setBookId(Integer bookId) {
        this.bookId = bookId;
    }

    public Integer getMemberId() {
        return memberId;
    }

    public void setMemberId(Integer memberId) {
        this.memberId = memberId;
    }

    public LocalDate getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(LocalDate issueDate) {
        this.issueDate = issueDate;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getBookTitle() {
        return bookTitle;
    }

    public void setBookTitle(String bookTitle) {
        this.bookTitle = bookTitle;
    }

    public String getBookAuthor() {
        return bookAuthor;
    }

    public void setBookAuthor(String bookAuthor) {
        this.bookAuthor = bookAuthor;
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

    public String getMemberDepartment() {
        return memberDepartment;
    }

    public void setMemberDepartment(String memberDepartment) {
        this.memberDepartment = memberDepartment;
    }

    public int getDaysOverdue() {
        return daysOverdue;
    }

    public void setDaysOverdue(int daysOverdue) {
        this.daysOverdue = daysOverdue;
    }

    public double getFineAmount() {
        return fineAmount;
    }

    public void setFineAmount(double fineAmount) {
        this.fineAmount = fineAmount;
    }

    public String getFineStatus() {
        return fineStatus;
    }

    public void setFineStatus(String fineStatus) {
        this.fineStatus = fineStatus;
    }

    public Integer getFineId() {
        return fineId;
    }

    public void setFineId(Integer fineId) {
        this.fineId = fineId;
    }

    @Override
    public String toString() {
        return "CirculationRecord{" +
                "issueId=" + issueId +
                ", bookId=" + bookId +
                ", memberId=" + memberId +
                ", issueDate=" + issueDate +
                ", dueDate=" + dueDate +
                ", returnDate=" + returnDate +
                ", status='" + status + '\'' +
                ", bookTitle='" + bookTitle + '\'' +
                ", memberName='" + memberName + '\'' +
                ", daysOverdue=" + daysOverdue +
                ", fineAmount=" + fineAmount +
                ", fineStatus='" + fineStatus + '\'' +
                '}';
    }
}
