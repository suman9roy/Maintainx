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
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository repository;
    private final MaintenanceClient maintenanceClient;

    public Expense addExpense(
            ExpenseRequest request, UUID apartmentId) {
        // Validate that the apartmentId in the request header matches the apartmentId in the request body
        if (!apartmentId.toString().equals(request.getApartmentId())) {
            throw new IllegalArgumentException("Apartment ID in request header does not match the apartment ID in the request body");
        }

        Expense expense = Expense.builder()
                .title(request.getTitle())
                .category(request.getCategory())
                .amount(request.getAmount())
                .description(request.getDescription())
                .expenseDate(request.getExpenseDate())
                .apartmentId(apartmentId)
                .build();

        return repository.save(expense);
    }

    public List<Expense> getAllExpenses(UUID apartmentId) {

        return repository.findAllByApartmentId(apartmentId);
    }

    public List<Expense> getByCategory(
            ExpenseCategory category, UUID apartmentId) {

        return repository.findByCategoryAndApartmentId(category, apartmentId);
    }


    public FundSummaryResponse getFundSummary(UUID apartmentId) {

        Double totalCollection =
                maintenanceClient
                        .getTotalCollectedAmount(apartmentId.toString());

        Double totalExpenses =
                repository.getTotalExpenses(apartmentId);

        return FundSummaryResponse.builder()
                .totalCollection(totalCollection)
                .totalExpenses(totalExpenses)
                .remainingFund(
                        totalCollection - totalExpenses
                )
                .build();
    }
}