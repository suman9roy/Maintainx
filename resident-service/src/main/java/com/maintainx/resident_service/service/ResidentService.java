package com.maintainx.resident_service.service;

import com.maintainx.resident_service.entity.Resident;
import com.maintainx.resident_service.entity.ResidentJoinRequest;
import com.maintainx.resident_service.enums.JoinRequestStatus;
import com.maintainx.resident_service.exception.InvalidRequestException;
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

    /**
     * Admin's list view — was findAll(), now scoped to the calling
     * admin's own apartment so admin A never sees apartment B's residents.
     */
    public List<Resident> getAllResidents(UUID adminApartmentId) {
        return residentRepository.findAllByApartmentId(adminApartmentId);
    }

    /**
     * Ownership-protected:
     *   ADMIN    → can fetch any resident, but only within their own apartment
     *   RESIDENT → can only fetch a record whose userId matches their JWT
     *              AND whose join request is APPROVED
     */
    public Resident getResident(Long id, UUID requestingUserId, String role, UUID requestingApartmentId) {

        Resident resident = residentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Resident not found with id: " + id
                ));

        if ("ADMIN".equals(role)) {
            if (requestingApartmentId == null || !resident.getApartmentId().equals(requestingApartmentId)) {
                throw new UnauthorizedAccessException(
                        "Access denied — this resident does not belong to your apartment"
                );
            }
            return resident;
        }

        if (!resident.getUserId().equals(requestingUserId)) {
            throw new UnauthorizedAccessException(
                    "Access denied — you do not have permission to view this resident profile"
            );
        }

        boolean approved = joinRequestRepository
                .findByUserId(requestingUserId)
                .stream()
                .anyMatch(r -> r.getFlatNumber().equals(resident.getFlatNumber())
                        && r.getApartmentId().equals(resident.getApartmentId())
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
     * Returns all APPROVED resident records for this user, across whichever
     * apartment(s) they've been approved into.
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
                        .anyMatch(req -> req.getFlatNumber().equals(r.getFlatNumber())
                                && req.getApartmentId().equals(r.getApartmentId())))
                .toList();
    }

    public void deleteResident(Long id, UUID adminApartmentId) {
        Resident resident = residentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resident not found with id: " + id));

        if (!resident.getApartmentId().equals(adminApartmentId)) {
            throw new UnauthorizedAccessException(
                    "Access denied — this resident does not belong to your apartment"
            );
        }
        residentRepository.deleteById(id);
    }

    public List<Resident> getResidentsByFlatNumber(String flatNumber, UUID adminApartmentId) {
        if (flatNumber == null || flatNumber.isEmpty()) {
            throw new InvalidRequestException("Flat number cannot be null or empty");
        }
        if (!residentRepository.existsByFlatNumberAndApartmentId(flatNumber, adminApartmentId)) {
            throw new ResourceNotFoundException("No residents found for flat number: " + flatNumber);
        }
        return residentRepository.findAllByFlatNumberAndApartmentId(flatNumber, adminApartmentId);
    }
}
