package com.booknest.backend.exception;

public class BookValidationException extends RuntimeException {
    public BookValidationException(String message) {
        super(message);
    }
}
