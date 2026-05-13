package com.maintainx.payment_service.dto;

import lombok.Data;

@Data
public class PaymentVerificationRequest {

    private String razorpayOrderId;

    private String razorpayPaymentId;

    private String razorpaySignature;
}