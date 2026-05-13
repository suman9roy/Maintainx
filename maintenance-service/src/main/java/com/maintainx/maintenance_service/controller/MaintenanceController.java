package com.maintainx.maintenance_service.controller;


import com.maintainx.maintenance_service.dto.MaintenanceRequest;
import com.maintainx.maintenance_service.entity.MaintenanceBill;
import com.maintainx.maintenance_service.service.MaintenanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/maintenance")
@RequiredArgsConstructor
public class MaintenanceController {

    private final MaintenanceService service;

    @PostMapping
    public MaintenanceBill generateBill(
            @RequestBody MaintenanceRequest request) {

        return service.generateBill(request);
    }

    @GetMapping
    public List<MaintenanceBill> getAllBills() {

        return service.getAllBills();
    }

    @GetMapping("/{flatNumber}")
    public List<MaintenanceBill> getBillsByFlat(
            @PathVariable String flatNumber) {

        return service.getBillsByFlat(flatNumber);
    }
    @GetMapping("/total-collected")
    public Double getTotalCollectedAmount() {

        return service.getTotalCollectedAmount();
    }
    @GetMapping("/bill/{id}")
    public MaintenanceBill getBill(
            @PathVariable Long id) {

        return service.getBill(id);
    }
    @PutMapping("/mark-paid/{id}")
    public String markPaid(
            @PathVariable Long id) {

        service.markAsPaid(id);

        return "Bill Marked As Paid";
    }
}