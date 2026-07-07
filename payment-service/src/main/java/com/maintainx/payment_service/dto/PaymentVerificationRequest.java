package com.maintainx.payment_service.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PaymentVerificationRequest {

    // All three fields are required — a blank value would cause
    // Utils.getHash() to throw a cryptic NullPointerException
    // before reaching the signature comparison logic.

    @NotBlank(message = "Razorpay order ID is required")
    private String razorpayOrderId;

    @NotBlank(message = "Razorpay payment ID is required")
    private String razorpayPaymentId;

    @NotBlank(message = "Razorpay signature is required")
    private String razorpaySignature;
}