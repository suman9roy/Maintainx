package com.maintainx.resident_service.dto;


import lombok.Data;

@Data
public class ResidentRequest {

    private String fullName;
    private String email;
    private String phoneNumber;
    private String flatNumber;
    private String blockName;
    private Integer floorNumber;
    private String residentType;
}