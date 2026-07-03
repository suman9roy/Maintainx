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
@Slf4j
@Service
@RequiredArgsConstructor
public class ComplaintService {

    private final ComplaintRepository repository;
    private final ResidentClient residentClient;

    public Complaint createComplaint(ComplaintRequest request, String userId, String role) {

        if (!"ADMIN".equals(role)) {
            log.info("The Resident summary is {}",residentClient.getResidentsForUser(userId, role));
            boolean ownsIdentity = getResidents(userId, role).stream()
                    .anyMatch(resident -> request.getResidentEmail().equalsIgnoreCase(resident.getEmail())
                            && request.getFlatNumber().equalsIgnoreCase(resident.getFlatNumber()));
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
                .flatNumber(request.getFlatNumber())
                .title(request.getTitle())
                .description(request.getDescription())
                .category(request.getCategory())
                .status(ComplaintStatus.OPEN)
                .createdAt(LocalDateTime.now())
                .build();

        return repository.save(complaint);
    }

    public List<Complaint> getAllComplaints() {
        return repository.findAll();
    }

    public List<Complaint> getByResident(String email, String userId, String role) {

        if (!"ADMIN".equals(role)) {
            boolean ownsEmail = getResidents(userId, role).stream()
                    .map(ResidentSummary::getEmail)
                    .anyMatch(email::equalsIgnoreCase);
            if (!ownsEmail) {
                throw new UnauthorizedAccessException("You can only view your own complaints");
            }
        }

        return repository.findByResidentEmail(email);
    }

    private List<ResidentSummary> getResidents(String userId, String role) {

        return residentClient.getResidentsForUser(userId, role);
    }

    public Complaint updateStatus(Long id, ComplaintStatusUpdateRequest request) {

        Complaint complaint = repository.findById(id)
                // Was: .orElseThrow() with no message — threw a bare
                // NoSuchElementException, also fell through to a generic 500.
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Complaint not found with id: " + id
                ));

        complaint.setStatus(request.getStatus());

        return repository.save(complaint);
    }
}