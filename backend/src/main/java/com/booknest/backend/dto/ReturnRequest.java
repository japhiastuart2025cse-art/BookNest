package com.booknest.backend.dto;

import java.time.LocalDate;

public class ReturnRequest {
    private LocalDate returnDate; // Optional; defaults to today

    public ReturnRequest() {
    }

    public ReturnRequest(LocalDate returnDate) {
        this.returnDate = returnDate;
    }

    public LocalDate getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(LocalDate returnDate) {
        this.returnDate = returnDate;
    }
}
