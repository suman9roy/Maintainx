package com.maintainx.expense_service.repository;


import com.maintainx.expense_service.entity.Expense;
import com.maintainx.expense_service.enums.ExpenseCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface ExpenseRepository
        extends JpaRepository<Expense, Long> {

    List<Expense> findByCategory(
            ExpenseCategory category);
    @Query("""
       SELECT COALESCE(SUM(e.amount),0)
       FROM Expense e
       """)
    Double getTotalExpenses();
}