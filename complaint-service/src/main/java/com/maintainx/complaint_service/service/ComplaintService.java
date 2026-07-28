package com.maintainx.complaint_service.service;

import com.maintainx.complaint_service.client.ResidentClient;
import com.maintainx.complaint_service.dto.ComplaintRequest;
import com.maintainx.complaint_service.dto.ComplaintStatusUpdateRequest;
import com.maintainx.complaint_service.dto.ResidentSummary;
import com.maintainx.complaint_service.entity.Complaint;
import com.maintainx.complaint_service.enums.ComplaintStatus;
import com.maintainx.complaint_service.exception.ResourceNotFoundException;
import com.maintainx.complaint_service.exception.UnauthorizedAccessException;
import com.maintainx.complaint_service.repository.ComplaintRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ComplaintService {

    private final ComplaintRepository repository;
    private final ResidentClient residentClient;

    public Complaint createComplaint(ComplaintRequest request, String userId, String role, UUID apartmentId ) {

        if (!"ADMIN".equals(role)) {
            log.info("The Resident summary is {}",residentClient.getResidentsForUser(userId, role));
            boolean ownsIdentity = getResidents(userId, role).stream()
                    .anyMatch(resident -> request.getResidentEmail().equalsIgnoreCase(resident.getEmail())
                            && request.getFlatNumber().equalsIgnoreCase(resident.getFlatNumber())
                            && request.getApartmentId().equals(resident.getApartmentId()));
            if (!ownsIdentity) {
                // Was: throw new SecurityException(...) — not caught by
                // GlobalExceptionHandler, fell through to a generic 500.
                // UnauthorizedAccessException maps to a proper 403.
                throw new UnauthorizedAccessException(
                        "You can only create complaints for your own approved flat"
                );
            }
        }

        Complaint complaint = Complaint.builder()
                .residentEmail(request.getResidentEmail())
                .apartmentId(apartmentId)
                .flatNumber(request.getFlatNumber())
                .title(request.getTitle())
                .description(request.getDescription())
                .category(request.getCategory())
                .status(ComplaintStatus.OPEN)
                .createdAt(LocalDateTime.now())
                .build();

        return repository.save(complaint);
    }

    public List<Complaint> getAllComplaints(UUID apartmentId) {
        return repository.findAllByApartmentId(apartmentId);
    }

    public List<Complaint> getByResident(String email, String userId, String role, UUID apartmentId) {

        if (!"ADMIN".equals(role)) {
            boolean ownsEmail = getResidents(userId, role).stream()
                    .map(ResidentSummary::getEmail)
                    .anyMatch(email::equalsIgnoreCase);
            if (!ownsEmail) {
                throw new UnauthorizedAccessException("You can only view your own complaints");
            }
        }else {
            // Admins can only view complaints for their own apartment
            List<ResidentSummary> residents = getResidents(userId, role);
            boolean ownsApartment = residents.stream()
                    .anyMatch(resident -> apartmentId.equals(resident.getApartmentId()));
            if (!ownsApartment) {
                throw new UnauthorizedAccessException("You can only view complaints for your own apartment");
            }
        }

        return repository.findByResidentEmailAndApartmentId(email, apartmentId);
    }

    private List<ResidentSummary> getResidents(String userId, String role) {

        return residentClient.getResidentsForUser(userId, role);
    }

    public Complaint updateStatus(Long id, ComplaintStatusUpdateRequest request, UUID apartmentId) {

        Complaint complaint = repository.findById(id)
                // Was: .orElseThrow() with no message — threw a bare
                // NoSuchElementException, also fell through to a generic 500.
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Complaint not found with id: " + id
                ));
        // Admins can only update complaints for their own apartment
        if (!complaint.getApartmentId().equals(apartmentId)) {
            throw new UnauthorizedAccessException(
                    "You do not have permission to update this complaint"
            );
        }

        complaint.setStatus(request.getStatus());

        return repository.save(complaint);
    }
}