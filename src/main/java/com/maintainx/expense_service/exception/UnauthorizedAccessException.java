package com.maintainx.expense_service.exception;

/**
 * Thrown when a caller is authenticated but not allowed to access/modify
 * a specific resource (e.g. a resident trying to view another resident's
 * profile, or pay another flat's bill). Distinct from the gateway's
 * role-based 403s — this is service-level OWNERSHIP enforcement.
 * Mapped to 403 Forbidden by GlobalExceptionHandler.
 *
 * auth-service doesn't throw this itself today, but it's included here
 * so the exception set is identical across every service — resident-service,
 * complaint-service, and maintenance-service all use it for their
 * ownership checks.
 */
public class UnauthorizedAccessException extends RuntimeException {
    public UnauthorizedAccessException(String message) {
        super(message);
    }
}