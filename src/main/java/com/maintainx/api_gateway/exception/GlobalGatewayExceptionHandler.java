package com.maintainx.api_gateway.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Handles routing-level failures in the gateway — errors that happen
 * BEFORE a request ever reaches a downstream service:
 *
 *   404 — no route matched the path (typo in URL, service not configured)
 *   503 — route matched but the service is not registered in Eureka
 *         (service is down, not started yet, or failed health check)
 *   500 — unexpected gateway-internal error
 *
 * WITHOUT this handler: Spring Boot's default Whitelabel error page (HTML)
 * is returned for all these cases — not useful for a React frontend.
 *
 * WITH this handler: consistent JSON is returned in the same ErrorResponse
 * shape used by all downstream services:
 * {
 *   "timestamp": "...",
 *   "status": 503,
 *   "error": "Service Unavailable",
 *   "message": "maintenance-service is currently unavailable. Please try again shortly.",
 *   "path": "/maintenance/bill/..."
 * }
 *
 * Note: this uses ErrorWebExceptionHandler (WebFlux) not @RestControllerAdvice
 * (Spring MVC) because the gateway runs on a reactive Netty server, not
 * a traditional servlet container. @RestControllerAdvice simply has no
 * effect in a WebFlux context.
 *
 * Order(-1) runs this AFTER Spring's DefaultErrorWebExceptionHandler
 * (Order(-2)) so we don't accidentally swallow framework-level errors.
 */
@Slf4j
@Order(-1)
@Component
@RequiredArgsConstructor
public class GlobalGatewayExceptionHandler implements ErrorWebExceptionHandler {

    private final ObjectMapper objectMapper;

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {

        ServerHttpResponse response = exchange.getResponse();
        String path = exchange.getRequest().getURI().getPath();

        HttpStatus status;
        String error;
        String message;

        if (ex instanceof NotFoundException) {
            // NotFoundException from Spring Cloud Gateway means the
            // service was found in the route table but Eureka has no
            // healthy instance registered right now
            status  = HttpStatus.SERVICE_UNAVAILABLE;
            error   = "Service Unavailable";
            message = "The requested service is currently unavailable. Please try again shortly.";
            log.error("Gateway 503 — no healthy instance for path {}: {}", path, ex.getMessage());

        } else if (ex instanceof ResponseStatusException rse) {
            // Covers 404 (no route matched) and other HTTP-level gateway errors
            status  = HttpStatus.resolve(rse.getStatusCode().value());
            if (status == null) status = HttpStatus.INTERNAL_SERVER_ERROR;
            error   = status.getReasonPhrase();
            message = switch (status) {
                case NOT_FOUND -> "The requested path '" + path + "' does not exist.";
                default        -> rse.getReason() != null ? rse.getReason() : ex.getMessage();
            };
            log.warn("Gateway {} on path {}: {}", status.value(), path, ex.getMessage());

        } else {
            // Unexpected gateway-internal error
            status  = HttpStatus.INTERNAL_SERVER_ERROR;
            error   = "Internal Gateway Error";
            message = "An unexpected error occurred in the gateway. Please try again later.";
            log.error("Unhandled gateway exception on path {}: ", path, ex);
        }

        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status",    status.value());
        body.put("error",     error);
        body.put("message",   message);
        body.put("path",      path);

        byte[] bytes;
        try {
            bytes = objectMapper.writeValueAsBytes(body);
        } catch (JsonProcessingException e) {
            bytes = ("{\"status\":500,\"message\":\"Gateway error\"}").getBytes();
        }

        DataBuffer buffer = response.bufferFactory().wrap(bytes);
        return response.writeWith(Mono.just(buffer));
    }
}