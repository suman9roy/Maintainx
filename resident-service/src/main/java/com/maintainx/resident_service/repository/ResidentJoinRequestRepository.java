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

    // Resident views their own requests (across any apartment they've applied to)
    List<ResidentJoinRequest> findByUserId(UUID userId);

    // Admin filters by status — scoped to their own apartment only
    List<ResidentJoinRequest> findByApartmentIdAndStatus(UUID apartmentId, JoinRequestStatus status);
    List<ResidentJoinRequest> findByApartmentId(UUID apartmentId);

    // Duplicate check: has this user already requested this flat in this apartment?
    boolean existsByUserIdAndFlatNumberAndApartmentIdAndStatus(
            UUID userId,
            String flatNumber,
            UUID apartmentId,
            JoinRequestStatus status
    );
}
