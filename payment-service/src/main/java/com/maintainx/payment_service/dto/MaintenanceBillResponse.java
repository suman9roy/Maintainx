package com.maintainx.payment_service.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class MaintenanceBillResponse {

    private UUID id;

    private String flatNumber;

    private BigDecimal amount;

    private String paymentStatus;
}