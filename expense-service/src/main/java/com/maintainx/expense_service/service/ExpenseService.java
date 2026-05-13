package com.maintainx.expense_service.service;

import com.maintainx.expense_service.client.MaintenanceClient;
import com.maintainx.expense_service.dto.ExpenseRequest;
import com.maintainx.expense_service.dto.FundSummaryResponse;
import com.maintainx.expense_service.entity.Expense;
import com.maintainx.expense_service.enums.ExpenseCategory;
import com.maintainx.expense_service.repository.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository repository;
    private final MaintenanceClient maintenanceClient;

    public Expense addExpense(
            ExpenseRequest request) {

        Expense expense = Expense.builder()
                .title(request.getTitle())
                .category(request.getCategory())
                .amount(request.getAmount())
                .description(request.getDescription())
                .expenseDate(request.getExpenseDate())
                .build();

        return repository.save(expense);
    }

    public List<Expense> getAllExpenses() {

        return repository.findAll();
    }

    public List<Expense> getByCategory(
            ExpenseCategory category) {

        return repository.findByCategory(category);
    }


    public FundSummaryResponse getFundSummary() {

        Double totalCollection =
                maintenanceClient
                        .getTotalCollectedAmount();

        Double totalExpenses =
                repository.getTotalExpenses();

        return FundSummaryResponse.builder()
                .totalCollection(totalCollection)
                .totalExpenses(totalExpenses)
                .remainingFund(
                        totalCollection - totalExpenses
                )
                .build();
    }
}