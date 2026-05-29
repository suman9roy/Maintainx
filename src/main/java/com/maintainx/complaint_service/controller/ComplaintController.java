package com.maintainx.complaint_service.controller;


import com.maintainx.complaint_service.dto.ComplaintRequest;
import com.maintainx.complaint_service.dto.ComplaintStatusUpdateRequest;
import com.maintainx.complaint_service.entity.Complaint;
import com.maintainx.complaint_service.service.ComplaintService;
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
            @RequestBody ComplaintRequest request) {

        return service.createComplaint(request);
    }

    @GetMapping
    public List<Complaint> getAllComplaints() {

        return service.getAllComplaints();
    }

    @GetMapping("/resident/{email}")
    public List<Complaint> getByResident(
            @PathVariable String email) {

        return service.getByResident(email);
    }

    @PutMapping("/{id}/status")
    public Complaint updateStatus(
            @PathVariable Long id,
            @RequestBody
            ComplaintStatusUpdateRequest request,
            @RequestHeader("X-User-Role")
            String role) {

        if (!role.equals("ADMIN")) {

            throw new RuntimeException(
                    "Only ADMIN can update complaint status"
            );
        }

        return service.updateStatus(id, request);
    }
}