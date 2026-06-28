package com.maintainx.maintenance_service.entity;

import com.maintainx.maintenance_service.enums.BillStatus;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "maintenance_bills")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MaintenanceBill {

    @Id
    @GeneratedValue
    @UuidGenerator                          // ✅ Hibernate-native UUID generation.
    //    IDENTITY (the previous strategy) only works
    //    with DB auto-increment numeric columns —
    //    Postgres has no auto-increment for UUID.
    @Column(updatable = false, nullable = false)
    private UUID id;

    private String flatNumber;
    private BigDecimal amount;
    private String month;
    private Integer year;
    private LocalDate dueDate;

    @Enumerated(EnumType.STRING)
    private BillStatus paymentStatus;
}