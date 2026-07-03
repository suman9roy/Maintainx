package com.maintainx.payment_service.exception;

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

    // ── 400 — DTO validation failures ────────────────────────────────────────
    // Fires when @Valid rejects CreateOrderRequest or PaymentVerificationRequest

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
    // e.g. bill already paid, invalid Razorpay signature

    @ExceptionHandler(InvalidRequestException.class)
    public ResponseEntity<ErrorResponse> handleInvalidRequest(
            InvalidRequestException ex, HttpServletRequest request) {

        log.warn("Invalid request on {}: {}", request.getRequestURI(), ex.getMessage());
        return build(HttpStatus.BAD_REQUEST, "Bad Request",
                ex.getMessage(), request, null);
    }

    // ── 404 — payment record not found ───────────────────────────────────────

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(
            ResourceNotFoundException ex, HttpServletRequest request) {

        log.warn("Not found on {}: {}", request.getRequestURI(), ex.getMessage());
        return build(HttpStatus.NOT_FOUND, "Not Found",
                ex.getMessage(), request, null);
    }

    // ── Feign failures — maintenance-service call ─────────────────────────────
    //
    // payment-service calls maintenance-service twice:
    //   1. getBill()      → during create-order (ownership + bill lookup)
    //   2. markBillAsPaid() → during verify (caught internally, not here)
    //
    // For call 1 — if maintenance-service returns an error, we relay the
    // status. A 403 from maintenance-service (flat not owned by this user)
    // should come back as 403 here, not a confusing 500.

    @ExceptionHandler(FeignException.class)
    public ResponseEntity<ErrorResponse> handleFeignException(
            FeignException ex, HttpServletRequest request) {

        HttpStatus status = HttpStatus.resolve(ex.status());
        if (status == null) status = HttpStatus.BAD_GATEWAY;

        log.error("Feign call to maintenance-service failed on {} — status {}: {}",
                request.getRequestURI(), ex.status(), ex.getMessage());

        String message = status.is5xxServerError()
                ? "Maintenance service is unavailable. Please try again shortly."
                : ex.getMessage();

        return build(status, status.getReasonPhrase(), message, request, null);
    }

    // ── Razorpay SDK failures ─────────────────────────────────────────────────
    //
    // RazorpayClient.orders.create() and Utils.getHash() can throw
    // com.razorpay.RazorpayException — a checked exception that wraps
    // HTTP errors from the Razorpay API (invalid key, rate limit, etc).
    // Without this handler, the `throws Exception` on the controller method
    // surfaces as a raw 500. This gives it a specific, actionable message.

    @ExceptionHandler(com.razorpay.RazorpayException.class)
    public ResponseEntity<ErrorResponse> handleRazorpayException(
            com.razorpay.RazorpayException ex, HttpServletRequest request) {

        log.error("Razorpay API error on {}: {}", request.getRequestURI(), ex.getMessage());
//need full stack trace for debugging, but not for user-facing message
        log.error("Full stack trace for debugging:", ex);
        return build(HttpStatus.BAD_GATEWAY, "Payment Gateway Error",
                "Payment gateway returned an error. Please try again or contact support.",
                request, null);
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