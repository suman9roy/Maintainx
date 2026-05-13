package com.maintainx.payment_service.dto;


import lombok.Data;

@Data
public class PaymentRequest {

    private String flatNumber;

    private Double amount;
}