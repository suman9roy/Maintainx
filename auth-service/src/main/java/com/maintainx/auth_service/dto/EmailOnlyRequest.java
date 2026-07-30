package com.maintainx.auth_service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Shared by /auth/resend-otp and /auth/forgot-password — both just need
 * an email address to kick off sending an OTP.
 */
@Data
public class EmailOnlyRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Must be a valid email address")
    private String email;
}
