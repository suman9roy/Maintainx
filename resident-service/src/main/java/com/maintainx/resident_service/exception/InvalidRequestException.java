package com.maintainx.resident_service.exception;

/**
 * Thrown when the request is structurally valid (passes DTO @Valid)
 * but violates a business rule — e.g. wrong file type, duplicate
 * pending request for the same flat, or trying to approve an
 * already-rejected request.
 * Mapped to 400 Bad Request by GlobalExceptionHandler.
 */
public class InvalidRequestException extends RuntimeException {
    public InvalidRequestException(String message) {
        super(message);
    }
}