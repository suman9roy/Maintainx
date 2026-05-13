package com.maintainx.expense_service.dto;
import com.maintainx.expense_service.enums.ExpenseCategory;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ExpenseRequest {

    private String title;

    private ExpenseCategory category;

    private Double amount;

    private String description;

    private LocalDate expenseDate;
}