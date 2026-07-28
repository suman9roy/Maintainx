package com.maintainx.auth_service.service;

import com.maintainx.auth_service.dto.OnboardApartmentRequest;
import com.maintainx.auth_service.entity.Apartment;
import com.maintainx.auth_service.entity.Users;
import com.maintainx.auth_service.exception.DuplicateResourceException;
import com.maintainx.auth_service.exception.ResourceNotFoundException;
import com.maintainx.auth_service.repository.ApartmentRepository;
import com.maintainx.auth_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SuperAdminService {

    private final ApartmentRepository apartmentRepository;
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder encoder;

    /**
     * Creates the Apartment and its first ADMIN user together. If either
     * write fails, both roll back — you never end up with an apartment
     * that has no admin, or an admin with no apartment.
     */
    @Transactional
    public Map<String, String> onboardApartment(OnboardApartmentRequest request) {

        if (apartmentRepository.existsByNameIgnoreCase(request.getApartmentName())) {
            throw new DuplicateResourceException(
                    "An apartment named '" + request.getApartmentName() + "' already exists"
            );
        }
        if (userRepository.existsByEmail(request.getAdminEmail())) {
            throw new DuplicateResourceException(
                    "A user already exists with email: " + request.getAdminEmail()
            );
        }

        Apartment apartment = Apartment.builder()
                .name(request.getApartmentName())
                .address(request.getAddress())
                .city(request.getCity())
                .build();
        Apartment savedApartment = apartmentRepository.save(apartment);

        Users admin = Users.builder()
                .name(request.getAdminName())
                .email(request.getAdminEmail())
                .password(encoder.encode(request.getAdminPassword()))
                .role("ADMIN")
                .apartmentId(savedApartment.getId())
                .build();
        Users savedAdmin = userRepository.save(admin);

        return Map.of(
                "message",      "Apartment onboarded successfully",
                "apartmentId",  savedApartment.getId().toString(),
                "adminUserId",  savedAdmin.getId().toString()
        );
    }

    public List<Apartment> listApartments() {
        return apartmentRepository.findAll();
    }

    @Transactional
    public void setApartmentActive(String apartmentId, boolean active) {
        Apartment apartment = apartmentRepository.findById(UUID.fromString(apartmentId))
                .orElseThrow(() -> new ResourceNotFoundException("Apartment not found: " + apartmentId));
        apartment.setActive(active);
        apartmentRepository.save(apartment);
    }
}
