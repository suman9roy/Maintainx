package com.maintainx.maintenance_service.repository;



import com.maintainx.maintenance_service.entity.MaintenanceBill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MaintenanceRepository
        extends JpaRepository<MaintenanceBill, Long> {

    List<MaintenanceBill>
    findByFlatNumber(String flatNumber);
    @Query("""
       SELECT COALESCE(SUM(m.amount),0)
       FROM MaintenanceBill m
       WHERE m.paymentStatus = 'PAID'
       """)
    Double getTotalCollectedAmount();
}