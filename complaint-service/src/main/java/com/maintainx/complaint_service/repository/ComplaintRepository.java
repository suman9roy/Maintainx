package com.maintainx.complaint_service.repository;


import com.maintainx.complaint_service.entity.Complaint;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ComplaintRepository
        extends JpaRepository<Complaint, Long> {

    List<Complaint> findByResidentEmail(
            String residentEmail);
}