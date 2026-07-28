package com.maintainx.maintenance_service.repository;



import com.maintainx.maintenance_service.entity.MaintenanceBill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface MaintenanceRepository
        extends JpaRepository<MaintenanceBill, UUID> {


    @Query("""
       SELECT COALESCE(SUM(m.amount),0)
       FROM MaintenanceBill m
       WHERE m.paymentStatus = 'PAID'
       AND m.apartmentId = :apartmentId
       """)
    Double getTotalCollectedAmount(@Param("apartmentId") UUID apartmentId);

    List<MaintenanceBill> findAllByApartmentId(UUID apartmentId);

    List<MaintenanceBill> findByFlatNumberAndApartmentId(String flatNumber, UUID apartmentId);
}