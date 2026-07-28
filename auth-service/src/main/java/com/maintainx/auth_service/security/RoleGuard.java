package com.maintainx.auth_service.security;

import com.maintainx.auth_service.exception.UnauthorizedAccessException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * auth-service today relies on the api-gateway / downstream services for
 * role checks, and its own SecurityConfig permits all requests. That's
 * fine for /auth/login and /auth/register (must be open), but endpoints
 * like "onboard a new apartment" must never be open — only a SUPER_ADMIN
 * token should be able to call them.
 *
 * This is a deliberately minimal, explicit check (not a global filter)
 * so it's obvious which endpoints require it. It reads/verifies the
 * bearer token itself rather than trusting any headers set upstream,
 * since a stray or malicious call could otherwise bypass the gateway.
 */
@Component
@RequiredArgsConstructor
public class RoleGuard {

    private final JwtUtil jwtUtil;

    public Claims requireRole(String authorizationHeader, String requiredRole) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new UnauthorizedAccessException("Missing or malformed Authorization header");
        }

        String token = authorizationHeader.substring("Bearer ".length());

        Claims claims;
        try {
            claims = jwtUtil.parseClaims(token);
        } catch (JwtException e) {
            throw new UnauthorizedAccessException("Invalid or expired token");
        }

        String role = claims.get("role", String.class);
        if (!requiredRole.equals(role)) {
            throw new UnauthorizedAccessException(
                    "This action requires " + requiredRole + " privileges"
            );
        }

        return claims;
    }
}
