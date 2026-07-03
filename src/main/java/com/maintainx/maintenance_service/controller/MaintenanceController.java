package com.maintainx.maintenance_service.controller;

import com.maintainx.maintenance_service.dto.MaintenanceRequest;
import com.maintainx.maintenance_service.dto.MarkBillPaidRequest;
import com.maintainx.maintenance_service.entity.MaintenanceBill;
import com.maintainx.maintenance_service.service.MaintenanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
@Slf4j
@RestController
@RequestMapping("/maintenance")
@RequiredArgsConstructor
public class MaintenanceController {

    private final MaintenanceService service;

    // @Valid triggers MaintenanceRequest field validation before
    // the method body runs — invalid requests return 400 immediately
    // via GlobalExceptionHandler.handleValidation()
    @PostMapping
    public MaintenanceBill generateBill(@Valid @RequestBody MaintenanceRequest request) {
        return service.generateBill(request);
    }

    @GetMapping
    public List<MaintenanceBill> getAllBills() {
        return service.getAllBills();
    }

    @GetMapping("/{flatNumber}")
    public List<MaintenanceBill> getBillsByFlat(
            @PathVariable String flatNumber,
            @RequestHeader("X-User-Id")   String userId,
            @RequestHeader("X-User-Role") String role) {

        return service.getBillsByFlat(flatNumber, userId, role);
    }

    @GetMapping("/total-collected")
    public Double getTotalCollectedAmount() {
        return service.getTotalCollectedAmount();
    }

    @GetMapping("/bill/{id}")
    public MaintenanceBill getBill(
            @PathVariable UUID id,
            @RequestHeader("X-User-Id")   String userId,
            @RequestHeader("X-User-Role") String role) {

        return service.getBill(id, userId, role);
    }

//    @PutMapping("/mark-paid/{id}")
//    public String markPaid(@PathVariable UUID id) {
//        service.markAsPaid(id);
//        return "Bill Marked As Paid";
//    }
    @PutMapping("/mark-paid/{id}")
    public MaintenanceBill markPaid(
            @PathVariable UUID id,
            @Valid @RequestBody MarkBillPaidRequest request,
            @RequestHeader("X-User-Id") String adminId) {
        log.info("Marking bill {} as paid by admin {}", id, adminId);
        return service.markAsPaid(
                id,
                request,
                UUID.fromString(adminId));
    }
}