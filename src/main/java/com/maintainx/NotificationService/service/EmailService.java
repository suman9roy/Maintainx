package com.maintainx.NotificationService.service;

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
                                       String flatNumber) {
        send(
                to,
                "MaintainX — Payment Successful",
                "Dear Resident,\n\n"
                        + "Your maintenance payment of ₹" + amount
                        + " for flat " + flatNumber + " was successful.\n\n"
                        + "Thank you,\nMaintainX Team"
        );
    }

    // ── Join request — approved ───────────────────────────────────────────────

    public void sendJoinRequestApprovedMail(String to,
                                            String fullName,
                                            String flatNumber) {
        send(
                to,
                "MaintainX — Flat Join Request Approved ✅",
                "Dear " + fullName + ",\n\n"
                        + "Great news! Your request to join flat " + flatNumber
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
                                            String reason) {
        send(
                to,
                "MaintainX — Flat Join Request Rejected ❌",
                "Dear " + fullName + ",\n\n"
                        + "Unfortunately your request to join flat " + flatNumber
                        + " has been rejected.\n\n"
                        + "Reason: " + (reason != null ? reason : "Not specified") + "\n\n"
                        + "If you believe this is a mistake, please contact your society admin.\n\n"
                        + "MaintainX Team"
        );
    }

    // ── private helper ────────────────────────────────────────────────────────

    private void send(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
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