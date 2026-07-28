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

        if (HttpMethod.OPTIONS.equals(method)) {
            return chain.filter(exchange);
        }

        if (!validator.isSecured.test(path)) {
            return chain.filter(exchange);
        }

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

        UUID   userId      = jwtUtil.extractUserId(token);
        String role        = jwtUtil.extractRole(token);
        String apartmentId = jwtUtil.extractApartmentId(token);   // NEW — null for SUPER_ADMIN

        if (isAdminOnly(path, method) && !"ADMIN".equals(role)) {
            return reject(exchange, HttpStatus.FORBIDDEN);
        }

        // Super-admin-only routes (e.g. /super-admin/**) are guarded inside
        // auth-service itself via RoleGuard, not here — the gateway still
        // forwards them through since they're outside the isAdminOnly()
        // path list above.

        ServerWebExchange mutated = exchange.mutate()
                .request(r -> r.headers(headers -> {
                    headers.remove("X-User-Id");
                    headers.remove("X-User-Role");
                    headers.remove("X-Apartment-Id");
                    headers.add("X-User-Id",   userId.toString());
                    headers.add("X-User-Role", role);
                    if (apartmentId != null) {
                        headers.add("X-Apartment-Id", apartmentId);
                    }
                }))
                .build();

        return chain.filter(mutated);
    }

    @Override
    public int getOrder() {
        return -1;
    }

    private boolean isAdminOnly(String path, HttpMethod method) {

        if (path.startsWith("/join-requests")) {
            if (HttpMethod.POST.equals(method)
                    && path.equals("/join-requests"))  return false;
            if (path.equals("/join-requests/my"))      return false;
            return true;
        }

        if (path.startsWith("/residents")) {
            if (HttpMethod.POST.equals(method))        return true;
            if (HttpMethod.DELETE.equals(method))      return true;
            if (HttpMethod.GET.equals(method)
                    && path.equals("/residents"))      return true;
            return false;
        }

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

        if (path.startsWith("/expenses")) {
            return HttpMethod.POST.equals(method);
        }

        if (path.startsWith("/notices")) {
            return HttpMethod.POST.equals(method);
        }

        if (path.startsWith("/complaints")) {
            if (HttpMethod.POST.equals(method))              return false;
            if (path.matches("/complaints/resident/.+"))     return false;
            return true;
        }

        return false;
    }

    private Mono<Void> reject(ServerWebExchange exchange, HttpStatus status) {
        exchange.getResponse().setStatusCode(status);
        return exchange.getResponse().setComplete();
    }
}
