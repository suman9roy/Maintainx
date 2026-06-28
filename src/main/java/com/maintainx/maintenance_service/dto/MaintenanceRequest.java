package com.maintainx.maintenance_service.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class MaintenanceRequest {

    @NotBlank(message = "Flat number is required")
    private String flatNumber;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be greater than zero")
    private BigDecimal amount;

    @NotBlank(message = "Month is required")
    @Size(max = 20, message = "Month must be under 20 characters")
    private String month;

    @NotNull(message = "Year is required")
    private Integer year;

    @NotNull(message = "Due date is required")
    @FutureOrPresent(message = "Due date must be today or a future date")
    private LocalDate dueDate;
}