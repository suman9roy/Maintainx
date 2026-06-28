package com.maintainx.complaint_service.dto;

import com.maintainx.complaint_service.enums.ComplaintCategory;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ComplaintRequest {

    @NotBlank(message = "Resident email is required")
    @Email(message = "Must be a valid email address")
    private String residentEmail;

    @NotBlank(message = "Flat number is required")
    private String flatNumber;

    @NotBlank(message = "Title is required")
    @Size(max = 150, message = "Title must be under 150 characters")
    private String title;

    @NotBlank(message = "Description is required")
    @Size(max = 2000, message = "Description must be under 2000 characters")
    private String description;

    @NotNull(message = "Category is required")
    private ComplaintCategory category;
}