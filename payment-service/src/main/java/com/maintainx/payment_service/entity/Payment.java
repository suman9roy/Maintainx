package com.maintainx.payment_service.entity;

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

    private String flatNumber;

    private Double amount;

    private String razorpayOrderId;

    private String razorpayPaymentId;

    private String paymentStatus;

    private LocalDateTime paymentDate;
}