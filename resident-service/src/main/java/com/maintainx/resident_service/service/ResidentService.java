package com.maintainx.resident_service.service;

import com.maintainx.resident_service.entity.Resident;
import com.maintainx.resident_service.entity.ResidentJoinRequest;
import com.maintainx.resident_service.enums.JoinRequestStatus;
import com.maintainx.resident_service.exception.ResourceNotFoundException;
import com.maintainx.resident_service.exception.UnauthorizedAccessException;
import com.maintainx.resident_service.repository.ResidentJoinRequestRepository;
import com.maintainx.resident_service.repository.ResidentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResidentService {

    private final ResidentRepository residentRepository;
    private final ResidentJoinRequestRepository joinRequestRepository;

    public List<Resident> getAllResidents() {
        return residentRepository.findAll();
    }

    /**
     * Ownership-protected:
     *   ADMIN  → can fetch any resident by id
     *   RESIDENT → can only fetch a record whose userId matches their JWT
     *              AND whose join request is APPROVED
     */
    public Resident getResident(Long id, UUID requestingUserId, String role) {

        Resident resident = residentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Resident not found with id: " + id
                ));

        if ("ADMIN".equals(role)) {
            return resident;
        }

        if (!resident.getUserId().equals(requestingUserId)) {
            // Don't reveal that the record exists — return 403, not 404,
            // so the caller can't enumerate valid ids via different responses.
            throw new UnauthorizedAccessException(
                    "Access denied — you do not have permission to view this resident profile"
            );
        }

        boolean approved = joinRequestRepository
                .findByUserId(requestingUserId)
                .stream()
                .anyMatch(r -> r.getFlatNumber().equals(resident.getFlatNumber())
                        && r.getStatus() == JoinRequestStatus.APPROVED);

        if (!approved) {
            throw new UnauthorizedAccessException(
                    "Your join request for flat " + resident.getFlatNumber()
                            + " is not yet approved. Please wait for admin verification."
            );
        }

        return resident;
    }

    /**
     * Returns all APPROVED resident records for this user.
     * PENDING/REJECTED are not shown here — use GET /join-requests/my instead.
     */
    public List<Resident> getResidentByUserId(UUID userId) {

        List<Resident> residents = residentRepository.findAllByUserId(userId);

        List<ResidentJoinRequest> approvedRequests = joinRequestRepository
                .findByUserId(userId)
                .stream()
                .filter(r -> r.getStatus() == JoinRequestStatus.APPROVED)
                .toList();

        return residents.stream()
                .filter(r -> approvedRequests.stream()
                        .anyMatch(req -> req.getFlatNumber().equals(r.getFlatNumber())))
                .toList();
    }

    public void deleteResident(Long id) {
        if (!residentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Resident not found with id: " + id);
        }
        residentRepository.deleteById(id);
    }
}