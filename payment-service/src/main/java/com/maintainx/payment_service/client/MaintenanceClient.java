package com.maintainx.payment_service.client;

import com.maintainx.payment_service.dto.MaintenanceBillResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

@FeignClient(name = "maintenance-service")
public interface MaintenanceClient {

    @GetMapping("/maintenance/bill/{id}")
    MaintenanceBillResponse getBill(
            @PathVariable Long id);

    @PutMapping("/maintenance/mark-paid/{id}")
    void markBillAsPaid(
            @PathVariable Long id);
}