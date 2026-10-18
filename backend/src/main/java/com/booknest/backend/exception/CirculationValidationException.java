package com.booknest.backend.exception;

public class CirculationValidationException extends RuntimeException {
    public CirculationValidationException(String message) {
        super(message);
    }
}
