package com.maintainx.payment_service.entity;

import com.maintainx.payment_service.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long maintenanceBillId;

    private String flatNumber;

    private Double amount;

    private String razorpayOrderId;

    private String razorpayPaymentId;

    private String razorpaySignature;

    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;

    private LocalDateTime paymentDate;
}