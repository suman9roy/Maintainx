package com.maintainx.maintenance_service.exception;

import feign.FeignException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ── 400 — DTO validation (@Valid on MaintenanceRequest) ──────────────────

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(error.getField(), error.getDefaultMessage());
        }
        log.warn("Validation failed on {}: {}", request.getRequestURI(), fieldErrors);
        return build(HttpStatus.BAD_REQUEST, "Validation Failed",
                "One or more fields are invalid", request, fieldErrors);
    }

    // ── 400 — business rule violations ───────────────────────────────────────
    // e.g. trying to mark an already-PAID bill as paid again

    @ExceptionHandler(InvalidRequestException.class)
    public ResponseEntity<ErrorResponse> handleInvalidRequest(
            InvalidRequestException ex, HttpServletRequest request) {

        log.warn("Invalid request on {}: {}", request.getRequestURI(), ex.getMessage());
        return build(HttpStatus.BAD_REQUEST, "Bad Request",
                ex.getMessage(), request, null);
    }

    // ── 403 — flat ownership / access violations ──────────────────────────────

    @ExceptionHandler(UnauthorizedAccessException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorizedAccess(
            UnauthorizedAccessException ex, HttpServletRequest request) {

        log.warn("Access denied on {}: {}", request.getRequestURI(), ex.getMessage());
        return build(HttpStatus.FORBIDDEN, "Forbidden",
                ex.getMessage(), request, null);
    }

    // ── 404 — bill not found ─────────────────────────────────────────────────

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(
            ResourceNotFoundException ex, HttpServletRequest request) {

        log.warn("Not found on {}: {}", request.getRequestURI(), ex.getMessage());
        return build(HttpStatus.NOT_FOUND, "Not Found",
                ex.getMessage(), request, null);
    }

    // ── Feign failures — resident-service call ────────────────────────────────
    //
    // maintenance-service calls resident-service to verify flat ownership.
    // Without this handler, any resident-service failure produces a
    // confusing 500 here. With it, the downstream status is relayed:
    //   resident-service 404 (no flats) → 404 here
    //   resident-service 503 (down)     → 502 Bad Gateway here

    @ExceptionHandler(FeignException.class)
    public ResponseEntity<ErrorResponse> handleFeignException(
            FeignException ex, HttpServletRequest request) {

        HttpStatus status = HttpStatus.resolve(ex.status());
        if (status == null) status = HttpStatus.BAD_GATEWAY;

        log.error("Feign call to resident-service failed on {} — status {}: {}",
                request.getRequestURI(), ex.status(), ex.getMessage());

        String message = status.is5xxServerError()
                ? "Could not verify flat ownership — resident service is unavailable. Try again shortly."
                : "Could not verify flat ownership: " + ex.getMessage();

        return build(status, status.getReasonPhrase(), message, request, null);
    }

    // ── 500 — catch-all ───────────────────────────────────────────────────────

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(
            Exception ex, HttpServletRequest request) {

        log.error("Unhandled exception on {}: ", request.getRequestURI(), ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error",
                "Something went wrong. Please try again later.", request, null);
    }

    // ── helper ────────────────────────────────────────────────────────────────

    private ResponseEntity<ErrorResponse> build(
            HttpStatus status, String error, String message,
            HttpServletRequest request, Map<String, String> fieldErrors) {

        return ResponseEntity.status(status).body(
                ErrorResponse.builder()
                        .timestamp(LocalDateTime.now())
                        .status(status.value())
                        .error(error)
                        .message(message)
                        .path(request.getRequestURI())
                        .fieldErrors(fieldErrors)
                        .build()
        );
    }
}