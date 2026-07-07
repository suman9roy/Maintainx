package com.maintainx.expense_service.exception;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Standard error response shape — every service in the system
 * should return errors in this exact format, so clients (and the
 * frontend) can handle errors consistently regardless of which
 * service produced them.
 *
 * fieldErrors is only populated for DTO validation failures
 * (e.g. {"email": "must be a well-formed email address"}).
 * It's null for every other error type.
 */
@Data
@Builder
public class ErrorResponse {

    private LocalDateTime timestamp;
    private int status;
    private String error;       // short category, e.g. "Not Found", "Bad Request"
    private String message;     // human-readable detail
    private String path;        // the request path that failed
    private Map<String, String> fieldErrors;   // only for validation errors, else null
}