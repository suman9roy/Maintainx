package com.maintainx.resident_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RejectRequestDto {

    @NotBlank(message = "Rejection reason is required — resident needs to know why")
    @Size(max = 500, message = "Reason must be under 500 characters")
    private String reason;
}