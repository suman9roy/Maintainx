package com.maintainx.maintenance_service.dto;

import com.maintainx.maintenance_service.enums.PaymentMode;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MarkBillPaidRequest {

    @NotNull
    private PaymentMode paymentMode;

    private String remarks;
}