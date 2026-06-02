package com.maintainx.NotificationService.event;


import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentSuccessEvent {

    private Long maintenanceBillId;

    private String flatNumber;

    private String residentEmail;

    private Double amount;

    private String paymentId;
}