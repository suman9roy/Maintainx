package com.maintainx.resident_service.exception;

import feign.FeignException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ── 400 — DTO validation failures ────────────────────────────────────────

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(error.getField(), error.getDefaultMessage());
        }
        // @AssertTrue method-level violations come back as non-field errors
        ex.getBindingResult().getGlobalErrors().forEach(error ->
                fieldErrors.put(error.getObjectName(), error.getDefaultMessage())
        );

        log.warn("Validation failed on {}: {}", request.getRequestURI(), fieldErrors);

        return build(HttpStatus.BAD_REQUEST, "Validation Failed",
                "One or more fields are invalid", request, fieldErrors);
    }

    // ── 400 — invalid business request ───────────────────────────────────────

    @ExceptionHandler(InvalidRequestException.class)
    public ResponseEntity<ErrorResponse> handleInvalidRequest(
            InvalidRequestException ex, HttpServletRequest request) {

        log.warn("Invalid request on {}: {}", request.getRequestURI(), ex.getMessage());

        return build(HttpStatus.BAD_REQUEST, "Bad Request", ex.getMessage(), request, null);
    }

    // ── 400 — file too large (multipart upload exceeds spring.servlet.multipart.max-file-size) ──

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleFileTooLarge(
            MaxUploadSizeExceededException ex, HttpServletRequest request) {

        log.warn("File upload too large on {}", request.getRequestURI());

        return build(HttpStatus.BAD_REQUEST, "Bad Request",
                "Uploaded file exceeds the maximum allowed size of 5MB", request, null);
    }

    // ── 403 — ownership / access violations ─────────────────────────────────

    @ExceptionHandler(UnauthorizedAccessException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorizedAccess(
            UnauthorizedAccessException ex, HttpServletRequest request) {

        log.warn("Access denied on {}: {}", request.getRequestURI(), ex.getMessage());

        return build(HttpStatus.FORBIDDEN, "Forbidden", ex.getMessage(), request, null);
    }

    // ── 404 — resource not found ─────────────────────────────────────────────

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(
            ResourceNotFoundException ex, HttpServletRequest request) {

        log.warn("Not found on {}: {}", request.getRequestURI(), ex.getMessage());

        return build(HttpStatus.NOT_FOUND, "Not Found", ex.getMessage(), request, null);
    }

    // ── 409 — duplicate resource ─────────────────────────────────────────────

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ErrorResponse> handleDuplicate(
            DuplicateResourceException ex, HttpServletRequest request) {

        log.warn("Duplicate resource on {}: {}", request.getRequestURI(), ex.getMessage());

        return build(HttpStatus.CONFLICT, "Conflict", ex.getMessage(), request, null);
    }

    // ── Feign errors — downstream service failures ───────────────────────────
    //
    // resident-service doesn't currently have outgoing Feign clients,
    // but complaint-service and maintenance-service call BACK into it
    // via /residents/byUserId. If that endpoint throws, their Feign
    // client receives it as a FeignException. This handler covers
    // any future outgoing Feign calls added to resident-service.
    //
    // Key point: we relay the original HTTP status from the downstream
    // service rather than always returning 500, so the caller gets a
    // meaningful status code (e.g. 404 if the downstream resource wasn't
    // found, not a confusing 500 from the calling service).

    @ExceptionHandler(FeignException.class)
    public ResponseEntity<ErrorResponse> handleFeignException(
            FeignException ex, HttpServletRequest request) {

        HttpStatus status = HttpStatus.resolve(ex.status());
        if (status == null) status = HttpStatus.BAD_GATEWAY;

        log.error("Feign call failed on {} — downstream status {}: {}",
                request.getRequestURI(), ex.status(), ex.getMessage());

        String message = status.is5xxServerError()
                ? "A downstream service is unavailable. Please try again shortly."
                : ex.getMessage();

        return build(status, status.getReasonPhrase(), message, request, null);
    }

    // ── 500 — unexpected errors ───────────────────────────────────────────────

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

        ErrorResponse body = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(error)
                .message(message)
                .path(request.getRequestURI())
                .fieldErrors(fieldErrors)
                .build();

        return ResponseEntity.status(status).body(body);
    }
}