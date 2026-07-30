package com.maintainx.auth_service.service;

import com.maintainx.auth_service.entity.EmailOtp;
import com.maintainx.auth_service.enums.OtpPurpose;
import com.maintainx.auth_service.exception.InvalidOtpException;
import com.maintainx.auth_service.kafka.OtpEventProducer;
import com.maintainx.auth_service.kafka.OtpNotificationEvent;
import com.maintainx.auth_service.repository.EmailOtpRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class OtpService {

    private final EmailOtpRepository otpRepository;
    private final BCryptPasswordEncoder encoder;
    private final OtpEventProducer otpEventProducer;

    private static final SecureRandom RANDOM = new SecureRandom();

    @Value("${otp.expiry-minutes:10}")
    private int expiryMinutes;

    /**
     * Generates a fresh 6-digit OTP, stores its BCrypt hash (never the
     * plaintext), and publishes an event so notification-service emails
     * it to the user. Each call creates a NEW row rather than reusing an
     * old one — verification always reads the most recently created row
     * for the (email, purpose) pair, so a resend naturally invalidates
     * any earlier code for the same purpose.
     */
    public void generateAndSendOtp(String email, String fullName, OtpPurpose purpose) {

        String otp = String.format("%06d", RANDOM.nextInt(1_000_000));

        EmailOtp record = EmailOtp.builder()
                .email(email)
                .otpHash(encoder.encode(otp))
                .purpose(purpose)
                .expiresAt(LocalDateTime.now().plusMinutes(expiryMinutes))
                .used(false)
                .createdAt(LocalDateTime.now())
                .build();

        otpRepository.save(record);

        otpEventProducer.publishOtp(
                OtpNotificationEvent.builder()
                        .email(email)
                        .fullName(fullName)
                        .otp(otp)
                        .expiryMinutes(expiryMinutes)
                        .purpose(purpose.name())
                        .build()
        );

        log.info("OTP generated for email={}, purpose={}, expiresInMinutes={}",
                email, purpose, expiryMinutes);
    }

    /**
     * Validates a submitted OTP against the most recent row for
     * (email, purpose). Throws InvalidOtpException — with a message safe
     * to show the user — if there's no OTP on file, it's expired,
     * already used, or doesn't match. On success, marks the row used so
     * it can't be replayed.
     */
    public void verifyOtp(String email, String otp, OtpPurpose purpose) {

        EmailOtp record = otpRepository
                .findTopByEmailAndPurposeOrderByCreatedAtDesc(email, purpose)
                .orElseThrow(() -> new InvalidOtpException(
                        "No OTP request found for this email. Please request a new OTP."
                ));

        if (record.isUsed()) {
            throw new InvalidOtpException(
                    "This OTP has already been used. Please request a new one."
            );
        }

        if (record.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new InvalidOtpException(
                    "This OTP has expired. Please request a new one."
            );
        }

        if (!encoder.matches(otp, record.getOtpHash())) {
            throw new InvalidOtpException("Invalid OTP. Please check and try again.");
        }

        record.setUsed(true);
        otpRepository.save(record);
    }
}
