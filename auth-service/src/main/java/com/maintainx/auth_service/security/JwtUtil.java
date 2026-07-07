package com.maintainx.auth_service.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtUtil {

    // Must be a Base64-encoded 256-bit key.
    // Generate with: openssl rand -base64 32
    @Value("${jwt.secret}")
    private String secret;

    private static final long EXPIRY_MS = 86_400_000L; // 24 hours

    private Key signingKey; // decoded once, reused for every token

    /**
     * Runs once at application startup — right after Spring injects
     * `secret` from .env. If the secret is malformed or too short,
     * the app refuses to start with a clear message, instead of
     * failing later on the first login attempt with a cryptic
     * stack trace or a silent wrong-key bug.
     */
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

    public String generateToken(UUID uuId, String role) {

        return Jwts.builder()
                .setSubject(uuId.toString())
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRY_MS))
                .signWith(signingKey)
                .compact();
    }
}