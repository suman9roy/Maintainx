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

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MaintenanceService {

    private final MaintenanceRepository repository;
    private final ResidentClient residentClient;

    public MaintenanceBill generateBill(MaintenanceRequest request, UUID apartmentId) {
        // Validate that the flat number belongs to an approved resident in the apartment
        List<ResidentResponse> residents = residentClient.getResidentsForApartment(apartmentId.toString());

        if (residents.stream().noneMatch(resident -> resident.getFlatNumber().equals(request.getFlatNumber()))) {
            throw new InvalidRequestException("Flat number does not belong to an approved resident in the apartment");
        }

        MaintenanceBill bill = MaintenanceBill.builder()
                .flatNumber(request.getFlatNumber())
                .amount(request.getAmount())
                .month(request.getMonth())
                .year(request.getYear())
                .dueDate(request.getDueDate())
                .paymentStatus(BillStatus.PENDING)
                .apartmentId(apartmentId)
                .build();

        return repository.save(bill);
    }

    public List<MaintenanceBill> getAllBills(UUID apartmentId) {
        return repository.findAllByApartmentId(apartmentId);
    }

    public List<MaintenanceBill> getBillsByFlat(
            String flatNumber, String userId, String role, UUID apartmentId) {

        if (!"ADMIN".equals(role)) {

            if (!isUserAssignedToSameFlat(flatNumber, userId, role, apartmentId)) {
                // Was: throw new RuntimeException(...) → fell through to 500
                // Now: UnauthorizedAccessException → 403 with a clear message
                throw new UnauthorizedAccessException(
                        "You are not an approved resident of flat: " + flatNumber
                );
            }
        }else{
            // For ADMIN, ensure the flat belongs to the same apartment
            List<ResidentResponse> residents = residentClient.getResidentsForApartment(apartmentId.toString());
            boolean flatBelongsToApartment = residents.stream()
                    .anyMatch(resident -> resident.getFlatNumber().equals(flatNumber));
            if (!flatBelongsToApartment) {
                throw new UnauthorizedAccessException(
                        "Flat number " + flatNumber + " does not belong to your apartment"
                );
            }
        }

        return repository.findByFlatNumberAndApartmentId(flatNumber, apartmentId);
    }

    public MaintenanceBill getBill(UUID id, String userId, String role, UUID apartmentId) {

        MaintenanceBill bill = repository.findById(id)
                // Was: throw new RuntimeException("Bill not found") → 500
                // Now: ResourceNotFoundException → 404
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Bill not found with id: " + id
                ));

        if ("ADMIN".equals(role)) {
            // Admins can only access bills for their own apartment
            if (!bill.getApartmentId().equals(apartmentId)) {
                throw new UnauthorizedAccessException(
                        "Access denied — this bill does not belong to your apartment"
                );
            }
            return bill;
        }

        if (!isUserAssignedToSameFlat(bill.getFlatNumber(), userId, role, apartmentId)) {
            throw new UnauthorizedAccessException(
                    "Access denied — this bill does not belong to your flat"
            );
        }

        return bill;
    }

    public Double getTotalCollectedAmount(UUID apartmentId) {

        return repository.getTotalCollectedAmount(apartmentId);
    }
    public MaintenanceBill markAsPaid(
            UUID billId,
            MarkBillPaidRequest request,
            UUID adminId,
            UUID apartmentId) {

        MaintenanceBill bill = repository.findById(billId)
                .orElseThrow(() -> new ResourceNotFoundException("Bill not found"));
        // Admins can only mark bills as paid for their own apartment
        if (!bill.getApartmentId().equals(apartmentId)) {
            throw new UnauthorizedAccessException(
                    "You do not have permission to mark this bill as paid"
            );
        }

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

    private boolean isUserAssignedToSameFlat(
            String flatNumber, String userId, String role, UUID apartmentId) {

        List<ResidentResponse> residents =
                residentClient.getResidentsForUser(userId, role);
        log.info("Retrieved residents for user {}: {}", userId, residents);

        if (residents.isEmpty()) {
            // Was: RuntimeException → 500
            // Now: UnauthorizedAccessException → 403
            throw new UnauthorizedAccessException(
                    "No approved flats found for this user — "
                            + "please ensure your join request is approved"
            );
        }


        return residents.stream()
                .anyMatch(resident -> resident.getFlatNumber().equals(flatNumber)
                        && apartmentId.equals(resident.getApartmentId()));
    }
}