package com.maintainx.complaint_service.service;

import com.maintainx.complaint_service.dto.ComplaintRequest;
import com.maintainx.complaint_service.dto.ComplaintStatusUpdateRequest;
import com.maintainx.complaint_service.entity.Complaint;
import com.maintainx.complaint_service.enums.ComplaintStatus;
import com.maintainx.complaint_service.repository.ComplaintRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ComplaintService {

    private final ComplaintRepository repository;

    public Complaint createComplaint(
            ComplaintRequest request) {

        Complaint complaint =
                Complaint.builder()
                        .residentEmail(
                                request.getResidentEmail()
                        )
                        .flatNumber(
                                request.getFlatNumber()
                        )
                        .title(request.getTitle())
                        .description(
                                request.getDescription()
                        )
                        .category(
                                request.getCategory()
                        )
                        .status(ComplaintStatus.OPEN)
                        .createdAt(
                                LocalDateTime.now()
                        )
                        .build();

        return repository.save(complaint);
    }

    public List<Complaint> getAllComplaints() {

        return repository.findAll();
    }

    public List<Complaint> getByResident(
            String email) {

        return repository.findByResidentEmail(email);
    }

    public Complaint updateStatus(
            Long id,
            ComplaintStatusUpdateRequest request) {

        Complaint complaint =
                repository.findById(id)
                        .orElseThrow();

        complaint.setStatus(
                request.getStatus()
        );

        return repository.save(complaint);
    }
}