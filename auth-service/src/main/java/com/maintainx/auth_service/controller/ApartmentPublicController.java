package com.maintainx.auth_service.controller;

import com.maintainx.auth_service.dto.ApartmentSummaryDto;
import com.maintainx.auth_service.repository.ApartmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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
}
