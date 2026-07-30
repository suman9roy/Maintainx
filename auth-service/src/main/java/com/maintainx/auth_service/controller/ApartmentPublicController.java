package com.maintainx.auth_service.controller;

import com.maintainx.auth_service.dto.ApartmentSummaryDto;
import com.maintainx.auth_service.entity.Apartment;
import com.maintainx.auth_service.repository.ApartmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Public, unauthenticated — a prospective resident needs to see which
 * apartments exist before they can pick one on the join-request form.
 * Only ACTIVE apartments are shown; a suspended apartment shouldn't
 * accept new applicants.
 */
@RestController
@RequestMapping("/apartments")
@RequiredArgsConstructor
public class ApartmentPublicController {

    private final ApartmentRepository apartmentRepository;

    @GetMapping("/public")
    public List<ApartmentSummaryDto> listActiveApartments() {
        return apartmentRepository.findAll().stream()
                .filter(a -> Boolean.TRUE.equals(a.getActive()))
                .map(a -> new ApartmentSummaryDto(a.getId(), a.getName(), a.getCity()))
                .toList();
    }

    /**
     * Used by resident-service (service-to-service, via Feign) to double
     * check an apartmentId submitted with a join request is both real
     * and currently active — closes the gap where a client could bypass
     * the filtered /public list above and submit a request for a
     * suspended (or nonexistent) apartment directly.
     */
    @GetMapping("/public/{id}/active")
    public Map<String, Boolean> checkActive(@PathVariable UUID id) {
        return apartmentRepository.findById(id)
                .map(a -> Map.of(
                        "exists", true,
                        "active", Boolean.TRUE.equals(a.getActive())
                ))
                .orElse(Map.of("exists", false, "active", false));
    }
}
