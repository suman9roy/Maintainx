package com.maintainx.expense_service.client;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "maintenance-service")
public interface MaintenanceClient {


    @GetMapping("/maintenance/total-collected")
    Double getTotalCollectedAmount();
}