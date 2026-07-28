package com.maintainx.expense_service.entity;


import com.maintainx.expense_service.enums.ExpenseCategory;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "expenses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
   @NotNull(message = "Apartment ID is required")
   @Column(name = "apartment_id")
    private UUID apartmentId;
    private String title;

    @Enumerated(EnumType.STRING)
    private ExpenseCategory category;

    private Double amount;

    private String description;

    private LocalDate expenseDate;
}