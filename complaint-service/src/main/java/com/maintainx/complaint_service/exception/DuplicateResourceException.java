package com.maintainx.complaint_service.exception;

/**
 * Thrown when trying to create something that already exists
 * (e.g. registering with an email already in use).
 * Mapped to 409 Conflict by GlobalExceptionHandler.
 */
public class DuplicateResourceException extends RuntimeException {
    public DuplicateResourceException(String message) {
        super(message);
    }
}