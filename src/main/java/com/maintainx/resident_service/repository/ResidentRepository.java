package com.maintainx.resident_service.repository;

import com.maintainx.resident_service.entity.Resident;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ResidentRepository extends JpaRepository<Resident,Long> {
}
