package com.maintainx.complaint_service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.UUID;

@Data
public class ResidentSummary {
    @NotBlank(message = "Resident email is required")
    @Email(message = "Must be a valid email address")
    private String email;
    @NotBlank(message = "Flat number is required")
    private String flatNumber;
    @NotBlank(message = "Apartment ID is required")
    private UUID apartmentId;
}
