package com.booknest.backend.exception;

public class FineNotFoundException extends RuntimeException {
    public FineNotFoundException(String message) {
        super(message);
    }
}
