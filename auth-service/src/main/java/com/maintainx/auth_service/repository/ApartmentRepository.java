package com.maintainx.auth_service.repository;

import com.maintainx.auth_service.entity.Apartment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ApartmentRepository extends JpaRepository<Apartment, UUID> {
    boolean existsByNameIgnoreCase(String name);
}
