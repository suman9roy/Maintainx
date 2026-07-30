package com.maintainx.auth_service.entity;

import com.maintainx.auth_service.enums.OtpPurpose;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * One row per OTP issued. The OTP itself is never stored in plaintext —
 * only a BCrypt hash — same treatment as passwords, since a 6-digit code
 * is short enough to be worth protecting if the DB is ever exposed.
 *
 * A single (email, purpose) pair can have multiple rows over time (old
 * ones are marked used=true or simply left to expire) — we always read
 * the most recently created row for a given (email, purpose) when
 * verifying.
 */
@Entity
@Table(name = "email_otp")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailOtp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String email;

    @Column(name = "otp_hash", nullable = false)
    private String otpHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OtpPurpose purpose;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Builder.Default
    @Column(nullable = false)
    private boolean used = false;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
