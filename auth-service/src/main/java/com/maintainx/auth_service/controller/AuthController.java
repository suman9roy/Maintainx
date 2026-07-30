package com.maintainx.auth_service.controller;

import com.maintainx.auth_service.dto.EmailOnlyRequest;
import com.maintainx.auth_service.dto.LoginRequest;
import com.maintainx.auth_service.dto.RegisterRequest;
import com.maintainx.auth_service.dto.ResetPasswordRequest;
import com.maintainx.auth_service.dto.VerifyOtpRequest;
import com.maintainx.auth_service.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public Map<String, String> register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public String login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    /**
     * Confirms the OTP sent at registration and unlocks login for this
     * account. Public — the whole point is the user isn't logged in yet.
     */
    @PostMapping("/verify-email")
    public Map<String, String> verifyEmail(@Valid @RequestBody VerifyOtpRequest request) {
        authService.verifyEmail(request.getEmail(), request.getOtp());
        return Map.of("message", "Email verified successfully. You can now log in.");
    }

    /**
     * Re-sends the email-verification OTP (e.g. the first one expired or
     * never arrived). Public, same reasoning as verify-email.
     */
    @PostMapping("/resend-otp")
    public Map<String, String> resendOtp(@Valid @RequestBody EmailOnlyRequest request) {
        authService.resendVerificationOtp(request.getEmail());
        return Map.of("message", "If an unverified account exists for this email, a new OTP has been sent.");
    }

    /**
     * Starts the forgot-password flow. Always returns the same generic
     * message regardless of whether the email exists — see
     * AuthService.forgotPassword() for why.
     */
    @PostMapping("/forgot-password")
    public Map<String, String> forgotPassword(@Valid @RequestBody EmailOnlyRequest request) {
        authService.forgotPassword(request.getEmail());
        return Map.of("message", "If an account exists for this email, a password reset OTP has been sent.");
    }

    /**
     * Completes the forgot-password flow: validates the OTP and sets the
     * new password. Public — this IS the mechanism for a logged-out user
     * to regain access, so it can't require a Bearer token.
     */
    @PostMapping("/reset-password")
    public Map<String, String> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return Map.of("message", "Password reset successfully. You can now log in with your new password.");
    }

    /**
     * Reissues a JWT for the calling user using their CURRENT DB state
     * (role, apartmentId) — no password required.
     *
     * This route is not in RouteValidator.openEndpoints, so the API
     * Gateway already required a valid, unexpired Bearer token before
     * forwarding here, and set X-User-Id from that token's subject
     * claim — we don't re-validate the token ourselves, we just trust
     * the gateway-populated header, same as every other authenticated
     * downstream service does.
     *
     * Typical use: a resident's join request gets approved while they're
     * still logged in with an old token that has no apartmentId claim.
     * The frontend calls this after detecting the approval so the user
     * gets a fresh token — with the new apartmentId — without having to
     * log out and log back in.
     */
    @PostMapping("/refresh")
    public String refresh(@RequestHeader("X-User-Id") String userId) {
        return authService.refreshToken(UUID.fromString(userId));
    }
}