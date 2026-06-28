package com.maintainx.notice_service.exception;

/**
 * Thrown when a lookup by ID/email/etc finds nothing.
 * Mapped to 404 Not Found by GlobalExceptionHandler.
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}