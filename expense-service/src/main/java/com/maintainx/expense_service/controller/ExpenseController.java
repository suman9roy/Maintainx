package com.maintainx.expense_service.controller;


import com.maintainx.expense_service.dto.ExpenseRequest;
import com.maintainx.expense_service.dto.FundSummaryResponse;
import com.maintainx.expense_service.entity.Expense;
import com.maintainx.expense_service.enums.ExpenseCategory;
import com.maintainx.expense_service.service.ExpenseService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/expenses")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService service;
    @PostMapping
    public Expense addExpense(
            @RequestBody ExpenseRequest request,
            @RequestHeader("X-User-Role")
            String role) {

        if (!role.equals("ADMIN")) {
            throw new RuntimeException(
                    "Only ADMIN can add expenses"
            );
        }

        return service.addExpense(request);
    }



    @GetMapping
    public List<Expense> getAllExpenses() {

        return service.getAllExpenses();
    }

    @GetMapping("/category/{category}")
    public List<Expense> getByCategory(
            @PathVariable ExpenseCategory category) {

        return service.getByCategory(category);
    }
    @GetMapping("/fund-summary")
    public FundSummaryResponse getFundSummary() {

        return service.getFundSummary();
    }
}