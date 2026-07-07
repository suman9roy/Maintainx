package com.maintainx.api_gateway.filter;

import com.maintainx.api_gateway.security.JwtUtil;
import com.maintainx.api_gateway.security.RouteValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private final RouteValidator validator;
    private final JwtUtil jwtUtil;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange,
                             GatewayFilterChain chain) {

        String path     = exchange.getRequest().getURI().getPath();
        HttpMethod method = exchange.getRequest().getMethod();

        // ── 0. Always pass through OPTIONS (CORS preflight) ───────────────────
        //
        // Browsers send an OPTIONS request BEFORE the actual request to check
        // if CORS is allowed. This must succeed WITHOUT an Authorization header.
        // CorsWebFilter (order -2) handles adding the CORS response headers.
        // If we checked JWT here, every preflight would get a 401 and the
        // browser would block the real request before it's even sent.
        if (HttpMethod.OPTIONS.equals(method)) {
            return chain.filter(exchange);
        }

        // ── 1. Skip public routes (/auth/register, /auth/login) ──────────────
        if (!validator.isSecured.test(path)) {
            return chain.filter(exchange);
        }

        // ── 2. Require Authorization header ──────────────────────────────────
        String authHeader = exchange.getRequest()
                .getHeaders()
                .getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return reject(exchange, HttpStatus.UNAUTHORIZED);
        }

        String token = authHeader.substring(7);

        if (!jwtUtil.validateToken(token)) {
            return reject(exchange, HttpStatus.UNAUTHORIZED);
        }

        // ── 3. Extract claims from verified JWT ───────────────────────────────
        UUID   userId = jwtUtil.extractUserId(token);
        String role   = jwtUtil.extractRole(token);

        // ── 4. Enforce RBAC ───────────────────────────────────────────────────
        if (isAdminOnly(path, method) && !"ADMIN".equals(role)) {
            return reject(exchange, HttpStatus.FORBIDDEN);
        }

        // ── 5. Strip forgeable headers, inject from verified JWT ──────────────
        ServerWebExchange mutated = exchange.mutate()
                .request(r -> r.headers(headers -> {
                    headers.remove("X-User-Id");
                    headers.remove("X-User-Role");
                    headers.add("X-User-Id",   userId.toString());
                    headers.add("X-User-Role", role);
                }))
                .build();

        return chain.filter(mutated);
    }

    @Override
    public int getOrder() {
        return -1;  // after CorsWebFilter (-2), before everything else
    }

    private boolean isAdminOnly(String path, HttpMethod method) {

        // ── JOIN REQUESTS ─────────────────────────────────────────────────────
        if (path.startsWith("/join-requests")) {
            if (HttpMethod.POST.equals(method)
                    && path.equals("/join-requests"))  return false;
            if (path.equals("/join-requests/my"))      return false;
            return true;
        }

        // ── RESIDENTS ─────────────────────────────────────────────────────────
        if (path.startsWith("/residents")) {
            if (HttpMethod.POST.equals(method))        return true;
            if (HttpMethod.DELETE.equals(method))      return true;
            if (HttpMethod.GET.equals(method)
                    && path.equals("/residents"))      return true;
            return false;
        }

        // ── MAINTENANCE ───────────────────────────────────────────────────────
        if (path.startsWith("/maintenance")) {
            if (HttpMethod.POST.equals(method))        return true;
            if (HttpMethod.PUT.equals(method)){
                return !path.matches("/maintenance/mark-paid/.*");
            }
            if (HttpMethod.GET.equals(method)) {
                if (path.equals("/maintenance"))                return true;
                if (path.equals("/maintenance/total-collected")) return true;
            }
            return false;
        }

        // ── EXPENSES ──────────────────────────────────────────────────────────
        if (path.startsWith("/expenses")) {
            return HttpMethod.POST.equals(method);
        }

        // ── NOTICES ───────────────────────────────────────────────────────────
        if (path.startsWith("/notices")) {
            return HttpMethod.POST.equals(method);
        }

        // ── COMPLAINTS ────────────────────────────────────────────────────────
        if (path.startsWith("/complaints")) {
            if (HttpMethod.POST.equals(method))              return false;
            if (path.matches("/complaints/resident/.+"))     return false;
            return true;
        }

        // ── PAYMENTS ──────────────────────────────────────────────────────────
        return false;
    }

    private Mono<Void> reject(ServerWebExchange exchange, HttpStatus status) {
        exchange.getResponse().setStatusCode(status);
        return exchange.getResponse().setComplete();
    }
}