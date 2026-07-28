package com.maintainx.expense_service.client;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@FeignClient(name = "maintenance-service")
public interface MaintenanceClient {


    @GetMapping("/maintenance/total-collected")
    Double getTotalCollectedAmount(@RequestHeader("X-Apartment-Id") String apartmentId);
}