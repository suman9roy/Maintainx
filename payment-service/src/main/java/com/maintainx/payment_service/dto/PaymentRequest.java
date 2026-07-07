package com.maintainx.payment_service.dto;


import lombok.Data;

import java.math.BigDecimal;

@Data
public class PaymentRequest {

    private String flatNumber;

    private BigDecimal amount;
}