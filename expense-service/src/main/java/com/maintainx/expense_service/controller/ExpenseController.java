package com.maintainx.expense_service.controller;

import com.maintainx.expense_service.dto.ExpenseRequest;
import com.maintainx.expense_service.dto.FundSummaryResponse;
import com.maintainx.expense_service.entity.Expense;
import com.maintainx.expense_service.enums.ExpenseCategory;
import com.maintainx.expense_service.service.ExpenseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/expenses")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService service;

    @PostMapping
    public Expense addExpense(@Valid @RequestBody ExpenseRequest request,
                                @RequestHeader("X-Apartment-Id") String apartmentId) {
        return service.addExpense(request, UUID.fromString(apartmentId));
    }

    @GetMapping
    public List<Expense> getAllExpenses(@RequestHeader("X-Apartment-Id") String apartmentId) {
        return service.getAllExpenses(UUID.fromString(apartmentId));
    }

    @GetMapping("/category/{category}")
    public List<Expense> getByCategory(@PathVariable ExpenseCategory category,
                                       @RequestHeader("X-Apartment-Id") String apartmentId) {
        return service.getByCategory(category, UUID.fromString(apartmentId));
    }

    @GetMapping("/fund-summary")
    public FundSummaryResponse getFundSummary(@RequestHeader("X-Apartment-Id") String apartmentId) {
        return service.getFundSummary(UUID.fromString(apartmentId));
    }
}