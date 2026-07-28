package com.maintainx.complaint_service.repository;


import com.maintainx.complaint_service.entity.Complaint;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ComplaintRepository
        extends JpaRepository<Complaint, Long> {


    List<Complaint> findAllByApartmentId(UUID apartmentId);

    List<Complaint> findByResidentEmailAndApartmentId(String email, UUID apartmentId);
}