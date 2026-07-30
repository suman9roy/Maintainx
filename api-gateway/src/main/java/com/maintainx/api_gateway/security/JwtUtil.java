package com.maintainx.api_gateway.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.UUID;

@Slf4j
@Component
public class JwtUtil {

    // Must match auth-service's jwt.secret exactly — same Base64 string
    @Value("${jwt.secret}")
    private String secret;

    private Key signingKey; // decoded once, reused for every request

    @PostConstruct
    public void init() {

        byte[] keyBytes;
        try {
            keyBytes = Decoders.BASE64.decode(secret);
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException(
                    "JWT_SECRET is not valid Base64. "
                            + "Generate one with: openssl rand -base64 32", e
            );
        }

        if (keyBytes.length < 32) {
            throw new IllegalStateException(
                    "JWT_SECRET decodes to only " + keyBytes.length
                            + " bytes; HS256 requires at least 32 bytes (256 bits). "
                            + "Regenerate with: openssl rand -base64 32 "
                            + "and copy the FULL string into .env"
            );
        }

        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
    }

    public boolean validateToken(String token) {
        try {
            Claims claims = extractAllClaims(token);
            log.info("JWT claims: {}", claims);
            return claims.getExpiration().getTime() > System.currentTimeMillis();
        } catch (Exception e) {
            log.warn("JWT validation failed: {}", e.getMessage());
            return false;
        }
    }

    public UUID extractUserId(String token) {
        return UUID.fromString(extractAllClaims(token).getSubject());
    }

    public String extractRole(String token) {
        return extractAllClaims(token).get("role", String.class);
    }

    /**
     * Returns null for SUPER_ADMIN tokens (they carry no apartmentId claim)
     * or for any token issued before this claim existed. Callers must
     * treat null as "no apartment scope" — never as "matches everything".
     */
    public String extractApartmentId(String token) {
        log.info("Extracting apartment ID from JWT token");
        String apartmentId = extractAllClaims(token).get("apartmentId", String.class);
        log.info("Extracted apartment ID: {}", apartmentId);
        return apartmentId;
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
