package com.maintainx.payment_service.dto;

import lombok.Data;
import jakarta.validation.constraints.NotNull;

@Data
public class MarkBillPaidRequest {

    @NotNull
    private PaymentMode paymentMode;

    private String remarks;
}

