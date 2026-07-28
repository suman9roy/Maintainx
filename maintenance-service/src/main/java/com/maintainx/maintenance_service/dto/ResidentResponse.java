package com.maintainx.maintenance_service.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class ResidentResponse{
    private String flatNumber;
    private UUID apartmentId;
}
