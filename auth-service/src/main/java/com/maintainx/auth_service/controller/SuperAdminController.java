package com.maintainx.auth_service.controller;

import com.maintainx.auth_service.dto.OnboardApartmentRequest;
import com.maintainx.auth_service.entity.Apartment;
import com.maintainx.auth_service.security.RoleGuard;
import com.maintainx.auth_service.service.SuperAdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/super-admin")
@RequiredArgsConstructor
public class SuperAdminController {

    private final SuperAdminService superAdminService;
    private final RoleGuard roleGuard;

    @PostMapping("/apartments")
    public Map<String, String> onboardApartment(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody OnboardApartmentRequest request) {

        roleGuard.requireRole(authHeader, "SUPER_ADMIN");
        return superAdminService.onboardApartment(request);
    }

    @GetMapping("/apartments")
    public List<Apartment> listApartments(
            @RequestHeader("Authorization") String authHeader) {

        roleGuard.requireRole(authHeader, "SUPER_ADMIN");
        return superAdminService.listApartments();
    }

    @PatchMapping("/apartments/{id}/suspend")
    public Map<String, String> suspendApartment(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable("id") String apartmentId) {

        roleGuard.requireRole(authHeader, "SUPER_ADMIN");
        superAdminService.setApartmentActive(apartmentId, false);
        return Map.of("message", "Apartment suspended");
    }

    @PatchMapping("/apartments/{id}/reactivate")
    public Map<String, String> reactivateApartment(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable("id") String apartmentId) {

        roleGuard.requireRole(authHeader, "SUPER_ADMIN");
        superAdminService.setApartmentActive(apartmentId, true);
        return Map.of("message", "Apartment reactivated");
    }
}
