package com.maintainx.expense_service.repository;


import com.maintainx.expense_service.entity.Expense;
import com.maintainx.expense_service.enums.ExpenseCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ExpenseRepository
        extends JpaRepository<Expense, Long> {

//need to add apartmentId to the query to get total expenses for a specific apartment
    @Query("""
       SELECT COALESCE(SUM(e.amount),0)
       FROM Expense e
       WHERE e.apartmentId = :apartmentId
       """)
    Double getTotalExpenses(@Param("apartmentId") UUID apartmentId);

    List<Expense> findAllByApartmentId(UUID apartmentId);

    List<Expense> findByCategoryAndApartmentId(ExpenseCategory category, UUID apartmentId);
}