package com.maintainx.resident_service.controller;

import com.maintainx.resident_service.dto.ResidentRequest;
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
     */
    @GetMapping
    public List<Resident> getAllResidents() {
        return service.getAllResidents();
    }

    /**
     * Any authenticated user can call this endpoint —
     * but the SERVICE verifies that a RESIDENT can only
     * see their own profile. ADMIN can see anyone.
     */
    @GetMapping("/byUserId")
    public List<Resident> getResident(
            @RequestHeader("X-User-Id")   String userId,
            @RequestHeader("X-User-Role") String role) {

        log.debug("getResidentByUserId called — userId={}, role={}", userId, role);
        return service.getResidentByUserId( UUID.fromString(userId));
    }
    @GetMapping("/{id}")
    public Resident getResident(
            @PathVariable Long id,
            @RequestHeader("X-User-Id")   String userId,
            @RequestHeader("X-User-Role") String role) {

        return service.getResident(id, UUID.fromString(userId), role);
    }

    /**
     * ADMIN only — gateway blocks RESIDENT before this is reached.
     */
    @DeleteMapping("/{id}")
    public String deleteResident(@PathVariable Long id) {
        service.deleteResident(id);
        return "Resident Deleted Successfully";
    }
}