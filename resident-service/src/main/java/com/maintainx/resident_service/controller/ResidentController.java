package com.maintainx.resident_service.controller;

import com.maintainx.resident_service.dto.ResidentRequest;
import com.maintainx.resident_service.entity.Resident;
import com.maintainx.resident_service.service.ResidentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/residents")
@RequiredArgsConstructor
public class ResidentController {

    private final ResidentService service;

    @PostMapping
    public Resident addResident(
            @RequestBody ResidentRequest request) {

        return service.addResident(request);
    }

    @GetMapping
    public List<Resident> getAllResidents() {

        return service.getAllResidents();
    }

    @GetMapping("/{id}")
    public Resident getResident(
            @PathVariable Long id) {

        return service.getResident(id);
    }

    @DeleteMapping("/{id}")
    public String deleteResident(
            @PathVariable Long id) {

        service.deleteResident(id);

        return "Resident Deleted Successfully";
    }
}