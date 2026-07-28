package com.maintainx.resident_service.controller;

import com.maintainx.resident_service.entity.Resident;
import com.maintainx.resident_service.service.ResidentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
@Slf4j
@RestController
@RequestMapping("/residents")
@RequiredArgsConstructor
public class ResidentController {

    private final ResidentService service;

    /**
     * ADMIN only — gateway blocks RESIDENT before this is reached.
     * Scoped to the calling admin's own apartment.
     */
    @GetMapping
    public List<Resident> getAllResidents(@RequestHeader("X-Apartment-Id") String apartmentId) {
        return service.getAllResidents(UUID.fromString(apartmentId));
    }

    @GetMapping("/byUserId")
    public List<Resident> getResident(
            @RequestHeader("X-User-Id")   String userId,
            @RequestHeader("X-User-Role") String role) {

        log.debug("getResidentByUserId called — userId={}, role={}", userId, role);
        return service.getResidentByUserId(UUID.fromString(userId));
    }

    @GetMapping("/{id}")
    public Resident getResident(
            @PathVariable Long id,
            @RequestHeader("X-User-Id")   String userId,
            @RequestHeader("X-User-Role") String role,
            @RequestHeader(value = "X-Apartment-Id", required = false) String apartmentId) {

        UUID apartmentUuid = apartmentId != null ? UUID.fromString(apartmentId) : null;
        return service.getResident(id, UUID.fromString(userId), role, apartmentUuid);
    }

    /**
     * ADMIN only — gateway blocks RESIDENT before this is reached.
     * Scoped to the calling admin's own apartment.
     */
    @DeleteMapping("/{id}")
    public String deleteResident(
            @PathVariable Long id,
            @RequestHeader("X-Apartment-Id") String apartmentId) {

        service.deleteResident(id, UUID.fromString(apartmentId));
        return "Resident Deleted Successfully";
    }

    @GetMapping("/byFlatNumber/{flatNumber}")
    public List<Resident> getResidentsByFlatNumber(
            @PathVariable String flatNumber,
            @RequestHeader("X-Apartment-Id") String apartmentId) {

        return service.getResidentsByFlatNumber(flatNumber, UUID.fromString(apartmentId));
    }
}
