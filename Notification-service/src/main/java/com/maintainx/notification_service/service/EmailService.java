package com.maintainx.notification_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    // ── Payment ───────────────────────────────────────────────────────────────

    public void sendPaymentSuccessMail(String to,
                                       BigDecimal amount,
                                       String flatNumber, String apartmentId) {
        send(
                to,
                "MaintainX — Payment Successful",
                "Dear Resident,\n\n"
                        + "Your maintenance payment of ₹" + amount
                        + " for flat " + flatNumber + " in apartment " + apartmentId + " was successful.\n\n"
                        + "Thank you,\nMaintainX Team"
        );
    }

    // ── Join request — approved ───────────────────────────────────────────────

    public void sendJoinRequestApprovedMail(String to,
                                            String fullName,
                                            String flatNumber, String apartmentId) {
        send(
                to,
                "MaintainX — Flat Join Request Approved ✅",
                "Dear " + fullName + ",\n\n"
                        + "Great news! Your request to join flat " + flatNumber + " in apartment " + apartmentId
                        + " has been approved by the admin.\n\n"
                        + "You can now log in and access your resident dashboard.\n\n"
                        + "Welcome to the society!\n"
                        + "MaintainX Team"
        );
    }

    // ── Join request — rejected ───────────────────────────────────────────────

    public void sendJoinRequestRejectedMail(String to,
                                            String fullName,
                                            String flatNumber,
                                            String reason, String apartmentId) {
        send(
                to,
                "MaintainX — Flat Join Request Rejected ❌",
                "Dear " + fullName + ",\n\n"
                        + "Unfortunately your request to join flat " + flatNumber + " in apartment " + apartmentId
                        + " has been rejected.\n\n"
                        + "Reason: " + (reason != null ? reason : "Not specified") + "\n\n"
                        + "If you believe this is a mistake, please contact your society admin.\n\n"
                        + "MaintainX Team"
        );
    }

    // ── Email verification OTP (registration) ───────────────────────────────

    public void sendEmailVerificationOtpMail(String to, String fullName, String otp, int expiryMinutes) {
        send(
                to,
                "MaintainX — Verify Your Email",
                "Dear " + fullName + ",\n\n"
                        + "Thanks for registering with MaintainX. Use the OTP below to verify your email address:\n\n"
                        + "    " + otp + "\n\n"
                        + "This OTP is valid for " + expiryMinutes + " minutes. You won't be able to log in until "
                        + "your email is verified.\n\n"
                        + "If you didn't create this account, you can safely ignore this email.\n\n"
                        + "MaintainX Team"
        );
    }

    // ── Password reset OTP (forgot password) ────────────────────────────────

    public void sendPasswordResetOtpMail(String to, String fullName, String otp, int expiryMinutes) {
        send(
                to,
                "MaintainX — Password Reset OTP",
                "Dear " + fullName + ",\n\n"
                        + "We received a request to reset your MaintainX password. Use the OTP below to proceed:\n\n"
                        + "    " + otp + "\n\n"
                        + "This OTP is valid for " + expiryMinutes + " minutes.\n\n"
                        + "If you didn't request a password reset, you can safely ignore this email — "
                        + "your password will not be changed.\n\n"
                        + "MaintainX Team"
        );
    }

    // ── private helper ────────────────────────────────────────────────────────

    private void send(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to.split(","));
            message.setSubject(subject);
            message.setText(text);
            mailSender.send(message);
            log.info("Email sent to {} — subject: {}", to, subject);
        } catch (Exception e) {
            // Log but don't crash — email failure should not break the main flow
            log.error("Failed to send email to {}: {}", to, e.getMessage());
        }
    }
}