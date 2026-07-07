package com.maintainx.payment_service.exception;

/**
 * Thrown when the request is structurally valid but violates a
 * business rule — e.g. bill already paid, invalid Razorpay signature.
 * Mapped to 400 Bad Request by GlobalExceptionHandler.
 */
public class InvalidRequestException extends RuntimeException {
    public InvalidRequestException(String message) {
        super(message);
    }
}