package com.booknest.backend.exception;

public class CirculationNotFoundException extends RuntimeException {
    public CirculationNotFoundException(String message) {
        super(message);
    }
}
