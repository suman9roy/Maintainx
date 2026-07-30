package com.maintainx.auth_service.service;


import com.maintainx.auth_service.dto.LoginRequest;
import com.maintainx.auth_service.dto.RegisterRequest;
import com.maintainx.auth_service.dto.ResetPasswordRequest;
import com.maintainx.auth_service.entity.Apartment;
import com.maintainx.auth_service.entity.Users;
import com.maintainx.auth_service.enums.OtpPurpose;
import com.maintainx.auth_service.exception.DuplicateResourceException;
import com.maintainx.auth_service.exception.EmailNotVerifiedException;
import com.maintainx.auth_service.exception.InvalidCredentialsException;
import com.maintainx.auth_service.exception.ResourceNotFoundException;
import com.maintainx.auth_service.exception.UnauthorizedAccessException;
import com.maintainx.auth_service.repository.ApartmentRepository;
import com.maintainx.auth_service.repository.UserRepository;
import com.maintainx.auth_service.security.JwtUtil;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository repository;
    private final ApartmentRepository apartmentRepository;
    private final BCryptPasswordEncoder encoder;
    private final JwtUtil jwtUtil;
    private final OtpService otpService;

    /**
     * Self-registration is only ever for residents, and residents don't
     * belong to an apartment yet at this point — that gets assigned when
     * an admin approves their join request against a specific apartment.
     * So apartmentId stays null here.
     *
     * The account is created with emailVerified=false and an OTP is sent
     * immediately — the user can't log in (see login() below) until they
     * verify via POST /auth/verify-email.
     */
    public Map<String, String> register(RegisterRequest request) {

        if (repository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException(
                    "User already exists with email: " + request.getEmail()
            );
        }

        Users user = Users.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(encoder.encode(request.getPassword()))
                .role("RESIDENT")           // role never taken from client
                .aadharNumber(request.getAadharNumber())
                .emailVerified(false)
                .build();

        Users saved = repository.save(user);

        otpService.generateAndSendOtp(saved.getEmail(), saved.getName(), OtpPurpose.EMAIL_VERIFICATION);

        return Map.of(
                "message", "User registered successfully. Please check your email for the OTP to verify your account.",
                "userId",  saved.getId().toString()
        );
    }

    public String login(LoginRequest request) {

        Users user = repository.findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found with email: " + request.getEmail()
                        ));

        if (!encoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        if (!user.isEmailVerified()) {
            throw new EmailNotVerifiedException(
                    "Please verify your email before logging in. "
                            + "Check your inbox for the OTP, or request a new one."
            );
        }

        assertApartmentUsable(user);

        return jwtUtil.generateToken(user.getId(), user.getRole(), user.getApartmentId());
    }

    /**
     * Verifies the OTP sent at registration and flips emailVerified to
     * true. Does nothing to the user's password or apartment — those are
     * unrelated to email ownership.
     */
    public void verifyEmail(String email, String otp) {

        Users user = repository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with email: " + email
                ));

        if (user.isEmailVerified()) {
            // Not an error — verifying twice is a harmless no-op from the
            // client's point of view (e.g. a double-submitted form).
            return;
        }

        otpService.verifyOtp(email, otp, OtpPurpose.EMAIL_VERIFICATION);

        user.setEmailVerified(true);
        repository.save(user);
    }

    /**
     * Re-sends a fresh verification OTP — e.g. the first one expired, or
     * the email never arrived. Silently no-ops if the account is already
     * verified rather than erroring, since the client doesn't need to
     * treat that as a failure.
     */
    public void resendVerificationOtp(String email) {

        Users user = repository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with email: " + email
                ));

        if (user.isEmailVerified()) {
            return;
        }

        otpService.generateAndSendOtp(user.getEmail(), user.getName(), OtpPurpose.EMAIL_VERIFICATION);
    }

    /**
     * Kicks off the forgot-password flow by emailing an OTP. Deliberately
     * does NOT reveal whether the email exists in the system — if it
     * doesn't, this silently no-ops so the endpoint can't be used to
     * enumerate registered accounts.
     */
    public void forgotPassword(String email) {

        repository.findByEmail(email).ifPresent(user ->
                otpService.generateAndSendOtp(user.getEmail(), user.getName(), OtpPurpose.PASSWORD_RESET)
        );
    }

    /**
     * Completes the forgot-password flow: validates the OTP, then sets
     * the new password. Unlike forgotPassword(), a missing user here IS
     * an error — you can't validate an OTP for an account that doesn't
     * exist, and the OTP itself already proves the caller received the
     * reset email, so there's no enumeration risk in confirming that.
     */
    public void resetPassword(ResetPasswordRequest request) {

        Users user = repository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with email: " + request.getEmail()
                ));

        otpService.verifyOtp(request.getEmail(), request.getOtp(), OtpPurpose.PASSWORD_RESET);

        user.setPassword(encoder.encode(request.getNewPassword()));
        repository.save(user);
    }

    /**
     * Reissues a token for an already-authenticated user, re-reading their
     * CURRENT row from the DB rather than trusting anything on the old
     * token. This is what lets a resident pick up a newly-approved
     * apartmentId (or any other role/apartment change) without having to
     * log out and log back in with their password.
     *
     * The caller (AuthController) only reaches this method after the API
     * Gateway has already validated the caller's existing JWT is
     * unexpired/untampered and resolved X-User-Id from it — so by the
     * time we get here we know "this is a currently-authenticated user",
     * we just don't know if their claims are stale.
     */
    public String refreshToken(UUID userId) {

        Users user = repository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found — your session may no longer be valid"
                ));

        assertApartmentUsable(user);

        return jwtUtil.generateToken(user.getId(), user.getRole(), user.getApartmentId());
    }

    /**
     * SUPER_ADMIN has no apartmentId — never blocked by this check.
     * ADMIN/RESIDENT with no apartmentId yet (not-yet-approved resident)
     * also pass through here — there's nothing to check against yet.
     */
    private void assertApartmentUsable(Users user) {
        if (user.getApartmentId() == null) {
            return;
        }

        Apartment apartment = apartmentRepository.findById(user.getApartmentId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Apartment not found for this account — contact support"
                ));

        if (!Boolean.TRUE.equals(apartment.getActive())) {
            throw new UnauthorizedAccessException(
                    "Your apartment's account has been suspended. "
                            + "Please contact your society administrator or MaintainX support."
            );
        }
    }
}
