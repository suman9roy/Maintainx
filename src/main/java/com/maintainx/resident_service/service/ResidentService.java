package com.maintainx.resident_service.service;



import com.maintainx.resident_service.dto.ResidentRequest;
import com.maintainx.resident_service.entity.Resident;
import com.maintainx.resident_service.repository.ResidentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ResidentService {

    private final ResidentRepository repository;

    public Resident addResident(
            ResidentRequest request) {

        Resident resident = Resident.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .flatNumber(request.getFlatNumber())
                .blockName(request.getBlockName())
                .floorNumber(request.getFloorNumber())
                .residentType(request.getResidentType())
                .build();

        return repository.save(resident);
    }

    public List<Resident> getAllResidents() {
        return repository.findAll();
    }

    public Resident getResident(Long id) {

        return repository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Resident not found"
                        ));
    }

    public void deleteResident(Long id) {
        repository.deleteById(id);
    }
}