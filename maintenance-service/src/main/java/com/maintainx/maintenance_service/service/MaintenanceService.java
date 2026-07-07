package com.maintainx.maintenance_service.service;

import com.maintainx.maintenance_service.client.ResidentClient;
import com.maintainx.maintenance_service.dto.MaintenanceRequest;
import com.maintainx.maintenance_service.dto.MarkBillPaidRequest;
import com.maintainx.maintenance_service.dto.ResidentResponse;
import com.maintainx.maintenance_service.entity.MaintenanceBill;
import com.maintainx.maintenance_service.enums.BillStatus;
import com.maintainx.maintenance_service.exception.InvalidRequestException;
import com.maintainx.maintenance_service.exception.ResourceNotFoundException;
import com.maintainx.maintenance_service.exception.UnauthorizedAccessException;
import com.maintainx.maintenance_service.repository.MaintenanceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MaintenanceService {

    private final MaintenanceRepository repository;
    private final ResidentClient residentClient;

    public MaintenanceBill generateBill(MaintenanceRequest request) {

        MaintenanceBill bill = MaintenanceBill.builder()
                .flatNumber(request.getFlatNumber())
                .amount(request.getAmount())
                .month(request.getMonth())
                .year(request.getYear())
                .dueDate(request.getDueDate())
                .paymentStatus(BillStatus.PENDING)
                .build();

        return repository.save(bill);
    }

    public List<MaintenanceBill> getAllBills() {
        return repository.findAll();
    }

    public List<MaintenanceBill> getBillsByFlat(
            String flatNumber, String userId, String role) {

        if (!"ADMIN".equals(role)) {
            if (!requireFlatAccess(flatNumber, userId, role)) {
                // Was: throw new RuntimeException(...) → fell through to 500
                // Now: UnauthorizedAccessException → 403 with a clear message
                throw new UnauthorizedAccessException(
                        "You are not an approved resident of flat: " + flatNumber
                );
            }
        }
        return repository.findByFlatNumber(flatNumber);
    }

    public MaintenanceBill getBill(UUID id, String userId, String role) {

        MaintenanceBill bill = repository.findById(id)
                // Was: throw new RuntimeException("Bill not found") → 500
                // Now: ResourceNotFoundException → 404
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Bill not found with id: " + id
                ));

        if ("ADMIN".equals(role)) {
            return bill;
        }

        if (!requireFlatAccess(bill.getFlatNumber(), userId, role)) {
            throw new UnauthorizedAccessException(
                    "Access denied — this bill does not belong to your flat"
            );
        }

        return bill;
    }

    public Double getTotalCollectedAmount() {

        return repository.getTotalCollectedAmount();
    }
    public MaintenanceBill markAsPaid(
            UUID billId,
            MarkBillPaidRequest request,
            UUID adminId) {

        MaintenanceBill bill = repository.findById(billId)
                .orElseThrow(() -> new ResourceNotFoundException("Bill not found"));

        if (bill.getPaymentStatus() == BillStatus.PAID) {
            throw new IllegalStateException("Bill already paid");
        }

        bill.setPaymentStatus(BillStatus.PAID);
        bill.setPaymentMode(request.getPaymentMode());
        bill.setDueDate(LocalDate.now());
       // bill.setAmount(BigDecimal.valueOf(1200));
        bill.setMonth(String.valueOf(LocalDate.now().getMonth()));
        bill.setYear(LocalDate.now().getYear());

        return repository.save(bill);
    }


    // ── private ───────────────────────────────────────────────────────────────

    private boolean requireFlatAccess(
            String flatNumber, String userId, String role) {

        List<ResidentResponse> residents =
                residentClient.getResidentsForUser(userId, role);

        if (residents.isEmpty()) {
            // Was: RuntimeException → 500
            // Now: UnauthorizedAccessException → 403
            throw new UnauthorizedAccessException(
                    "No approved flats found for this user — "
                            + "please ensure your join request is approved"
            );
        }

        return residents.stream()
                .anyMatch(r -> r.getFlatNumber().equals(flatNumber));
    }
}