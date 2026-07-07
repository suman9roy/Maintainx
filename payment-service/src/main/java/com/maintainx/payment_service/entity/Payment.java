package com.maintainx.payment_service.entity;

import com.maintainx.payment_service.enums.BillSyncStatus;
import com.maintainx.payment_service.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

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
    private Long id;   // Payment's own PK stays Long — IDENTITY is valid here,
    // this entity's id has nothing to do with MaintenanceBill's id

    // Changed from Long to UUID — must match MaintenanceBill.id exactly
    private UUID maintenanceBillId;

    private String residentEmail;
    private String flatNumber;
    private BigDecimal amount;

    private String razorpayOrderId;
    private String razorpayPaymentId;
    private String razorpaySignature;

    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;

    @Enumerated(EnumType.STRING)
    private BillSyncStatus billSyncStatus;

    @Builder.Default
    private int retryCount = 0;

    private LocalDateTime paymentDate;
}