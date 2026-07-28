package com.maintainx.payment_service.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class ResidentResponse {

    private String name;
    private UUID ApartmentId;
    private String flatNumber;

    private String email;

    private String phoneNumber;
}
