package com.maintainx.payment_service.dto;

import lombok.Data;

@Data
public class MaintenanceBillResponse {

    private Long id;

    private String flatNumber;

    private Double amount;

    private String paymentStatus;
}