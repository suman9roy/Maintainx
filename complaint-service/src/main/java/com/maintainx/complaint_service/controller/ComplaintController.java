package com.maintainx.complaint_service.controller;

import com.maintainx.complaint_service.dto.ComplaintRequest;
import com.maintainx.complaint_service.dto.ComplaintStatusUpdateRequest;
import com.maintainx.complaint_service.entity.Complaint;
import com.maintainx.complaint_service.service.ComplaintService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/complaints")
@RequiredArgsConstructor
public class ComplaintController {

    private final ComplaintService service;

    @PostMapping
    public Complaint createComplaint(
            @Valid @RequestBody ComplaintRequest request,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Role") String role,
            @RequestHeader("X-Apartment-Id") String apartmentId) {

        return service.createComplaint(request, userId, role, UUID.fromString(apartmentId));
    }

    @GetMapping
    public List<Complaint> getAllComplaints(@RequestHeader("X-Apartment-Id") String apartmentId) {
        return service.getAllComplaints(UUID.fromString(apartmentId));
    }

    @GetMapping("/resident/{email}")
    public List<Complaint> getByResident(
            @PathVariable String email,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Role") String role,
            @RequestHeader("X-Apartment-Id") String apartmentId) {

        return service.getByResident(email, userId, role, UUID.fromString(apartmentId));
    }

    @PutMapping("/{id}/status")
    public Complaint updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody ComplaintStatusUpdateRequest request,
            @RequestHeader("X-Apartment-Id") String apartmentId) {

        return service.updateStatus(id, request, UUID.fromString(apartmentId));
    }
}