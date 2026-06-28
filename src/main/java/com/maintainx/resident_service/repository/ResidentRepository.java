package com.maintainx.resident_service.repository;


import com.maintainx.resident_service.entity.Resident;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ResidentRepository extends JpaRepository<Resident, Long> {

    // One user can have multiple resident records (multiple flats)
    List<Resident> findAllByUserId(UUID userId);

    // Admin checks if flat+residentType combo already exists
    boolean existsByFlatNumberAndResidentType(
            String flatNumber,
            com.maintainx.resident_service.enums.ResidentType residentType
    );
}