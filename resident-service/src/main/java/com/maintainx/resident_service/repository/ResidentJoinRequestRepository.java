package com.maintainx.resident_service.repository;


import com.maintainx.resident_service.entity.ResidentJoinRequest;
import com.maintainx.resident_service.enums.JoinRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ResidentJoinRequestRepository
        extends JpaRepository<ResidentJoinRequest, Long> {

    // Resident views their own requests
    List<ResidentJoinRequest> findByUserId(UUID userId);

    // Admin filters by status — PENDING / APPROVED / REJECTED
    List<ResidentJoinRequest> findByStatus(JoinRequestStatus status);

    // Duplicate check: has this user already requested this flat?
    boolean existsByUserIdAndFlatNumberAndStatus(
            UUID userId,
            String flatNumber,
            JoinRequestStatus status
    );
}