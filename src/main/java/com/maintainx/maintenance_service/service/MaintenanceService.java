package com.maintainx.maintenance_service.service;



import com.maintainx.maintenance_service.dto.MaintenanceRequest;
import com.maintainx.maintenance_service.entity.MaintenanceBill;
import com.maintainx.maintenance_service.repository.MaintenanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MaintenanceService {

    private final MaintenanceRepository repository;

    public MaintenanceBill generateBill(
            MaintenanceRequest request) {

        MaintenanceBill bill =
                MaintenanceBill.builder()
                        .flatNumber(request.getFlatNumber())
                        .amount(request.getAmount())
                        .month(request.getMonth())
                        .year(request.getYear())
                        .dueDate(request.getDueDate())
                        .paymentStatus("PENDING")
                        .build();

        return repository.save(bill);
    }

    public List<MaintenanceBill> getAllBills() {

        return repository.findAll();
    }

    public List<MaintenanceBill> getBillsByFlat(
            String flatNumber) {

        return repository.findByFlatNumber(flatNumber);
    }
    public Double getTotalCollectedAmount() {

        return repository.getTotalCollectedAmount();
    }

    public MaintenanceBill getBill(Long id) {
        return repository.findById(id).orElseThrow(()->new RuntimeException("nothing is found"));
    }

    public void markAsPaid(Long id) {

        MaintenanceBill bill =
                repository.findById(id)
                        .orElseThrow();

        bill.setPaymentStatus("PAID");

        repository.save(bill);
    }
}