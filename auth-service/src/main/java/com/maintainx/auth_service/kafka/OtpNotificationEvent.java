package com.maintainx.auth_service.kafka;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Published on otp-notification-topic. Mirrored by
 * com.maintainx.notification_service.event.OtpNotificationEvent — field
 * names must stay identical for Jackson deserialization to work.
 *
 * Carries the OTP in PLAINTEXT — this is the one place it has to exist
 * unhashed, since the email needs to show it to the user. auth-service's
 * own DB only ever stores the BCrypt hash (see EmailOtp.otpHash).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OtpNotificationEvent {

    private String email;
    private String fullName;
    private String otp;
    private int expiryMinutes;
    private String purpose; // "EMAIL_VERIFICATION" | "PASSWORD_RESET"
}
