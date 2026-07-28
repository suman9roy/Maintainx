package com.maintainx.resident_service.repository;


import com.maintainx.resident_service.entity.Resident;
import com.maintainx.resident_service.enums.ResidentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ResidentRepository extends JpaRepository<Resident, Long> {

    List<Resident> findAllByUserId(UUID userId);

    // Admin's list view — scoped to their own apartment only
    List<Resident> findAllByApartmentId(UUID apartmentId);

    // Flat numbers are only unique WITHIN an apartment, not globally —
    // Sunrise Residency's "B204" and Palm Heights' "B204" are different flats.
    boolean existsByFlatNumberAndResidentTypeAndApartmentId(
            String flatNumber,
           ResidentType residentType,
            UUID apartmentId
    );

    List<Resident> findAllByFlatNumberAndApartmentId(String flatNumber, UUID apartmentId);

    boolean existsByFlatNumberAndApartmentId(String flatNumber, UUID apartmentId);
}
