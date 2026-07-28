package com.maintainx.auth_service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Submitted by SUPER_ADMIN to onboard a brand-new apartment/society
 * and create its first ADMIN account in one step.
 */
@Data
public class OnboardApartmentRequest {

    @NotBlank(message = "Apartment name is required")
    private String apartmentName;

    private String address;
    private String city;

    @NotBlank(message = "Admin name is required")
    private String adminName;

    @NotBlank(message = "Admin email is required")
    @Email(message = "Must be a valid email address")
    private String adminEmail;

    @NotBlank(message = "Admin password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String adminPassword;
}
