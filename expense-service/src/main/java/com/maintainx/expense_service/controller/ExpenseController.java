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

    /**
     * X-Apartment-Id is OPTIONAL on all three GET endpoints below — a
     * resident whose join request is still pending has no apartmentId
     * yet, and visiting "Society Expenses" in that state is normal, not
     * an error. There's no apartment to report on yet, so return an
     * empty/zeroed result instead of 500ing.
     */
    @GetMapping
    public List<Expense> getAllExpenses(
            @RequestHeader(value = "X-Apartment-Id", required = false) String apartmentId) {
        if (apartmentId == null) {
            return List.of();
        }
        return service.getAllExpenses(UUID.fromString(apartmentId));
    }

    @GetMapping("/category/{category}")
    public List<Expense> getByCategory(@PathVariable ExpenseCategory category,
                                       @RequestHeader(value = "X-Apartment-Id", required = false) String apartmentId) {
        if (apartmentId == null) {
            return List.of();
        }
        return service.getByCategory(category, UUID.fromString(apartmentId));
    }

    @GetMapping("/fund-summary")
    public FundSummaryResponse getFundSummary(
            @RequestHeader(value = "X-Apartment-Id", required = false) String apartmentId) {
        if (apartmentId == null) {
            return FundSummaryResponse.builder()
                    .totalCollection(0.0)
                    .totalExpenses(0.0)
                    .remainingFund(0.0)
                    .build();
        }
        return service.getFundSummary(UUID.fromString(apartmentId));
    }
}