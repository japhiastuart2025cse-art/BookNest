package com.booknest.backend.dto;

public class FinePaymentRequest {
    private String paymentNote;

    public FinePaymentRequest() {
    }

    public FinePaymentRequest(String paymentNote) {
        this.paymentNote = paymentNote;
    }

    public String getPaymentNote() {
        return paymentNote;
    }

    public void setPaymentNote(String paymentNote) {
        this.paymentNote = paymentNote;
    }
}
