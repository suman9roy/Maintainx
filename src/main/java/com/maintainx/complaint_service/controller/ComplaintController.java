package com.maintainx.complaint_service.controller;

import com.maintainx.complaint_service.dto.ComplaintRequest;
import com.maintainx.complaint_service.dto.ComplaintStatusUpdateRequest;
import com.maintainx.complaint_service.entity.Complaint;
import com.maintainx.complaint_service.service.ComplaintService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/complaints")
@RequiredArgsConstructor
public class ComplaintController {

    private final ComplaintService service;

    @PostMapping
    public Complaint createComplaint(
            @Valid @RequestBody ComplaintRequest request,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Role") String role) {

        return service.createComplaint(request, userId, role);
    }

    @GetMapping
    public List<Complaint> getAllComplaints() {
        return service.getAllComplaints();
    }

    @GetMapping("/resident/{email}")
    public List<Complaint> getByResident(
            @PathVariable String email,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Role") String role) {

        return service.getByResident(email, userId, role);
    }

    @PutMapping("/{id}/status")
    public Complaint updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody ComplaintStatusUpdateRequest request) {

        return service.updateStatus(id, request);
    }
}