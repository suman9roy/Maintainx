package com.maintainx.expense_service.dto;

import com.maintainx.expense_service.enums.ExpenseCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ExpenseRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 150, message = "Title must be under 150 characters")
    private String title;

    @NotNull(message = "Category is required")
    private ExpenseCategory category;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be greater than zero")
    private Double amount;

    @Size(max = 2000, message = "Description must be under 2000 characters")
    private String description;

    @NotNull(message = "Expense date is required")
    @PastOrPresent(message = "Expense date cannot be in the future")
    private LocalDate expenseDate;
}