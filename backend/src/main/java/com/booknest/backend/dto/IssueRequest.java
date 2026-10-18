package com.booknest.backend.dto;

public class IssueRequest {
    private Integer bookId;
    private Integer memberId;
    private Integer loanDays; // Sensible default e.g. 14 days

    public IssueRequest() {
    }

    public IssueRequest(Integer bookId, Integer memberId, Integer loanDays) {
        this.bookId = bookId;
        this.memberId = memberId;
        this.loanDays = loanDays;
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

    public Integer getLoanDays() {
        return loanDays;
    }

    public void setLoanDays(Integer loanDays) {
        this.loanDays = loanDays;
    }
}
