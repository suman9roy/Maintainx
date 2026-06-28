package com.maintainx.payment_service.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class CreateOrderRequest {

    @NotNull(message = "Maintenance bill ID is required")
    private UUID maintenanceBillId;
}