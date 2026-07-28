package com.maintainx.auth_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

/**
 * Deliberately minimal — this is shown to unauthenticated visitors picking
 * an apartment to apply to, so it must never include admin emails, exact
 * address, or anything else that isn't needed for that choice.
 */
@Data
@AllArgsConstructor
public class ApartmentSummaryDto {
    private UUID id;
    private String name;
    private String city;
}
