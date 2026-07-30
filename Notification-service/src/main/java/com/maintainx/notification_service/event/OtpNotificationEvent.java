package com.maintainx.notification_service.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Mirrors com.maintainx.auth_service.kafka.OtpNotificationEvent — field
 * names must stay identical for Jackson deserialization to work.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OtpNotificationEvent {

    private String email;
    private String fullName;
    private String otp;
    private int expiryMinutes;
    private String purpose; // "EMAIL_VERIFICATION" | "PASSWORD_RESET"
}
