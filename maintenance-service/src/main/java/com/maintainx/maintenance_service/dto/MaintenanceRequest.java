package com.maintainx.maintenance_service.dto;


import lombok.Data;

import java.time.LocalDate;

@Data
public class MaintenanceRequest {

    private String flatNumber;
    private Double amount;
    private String month;
    private Integer year;
    private LocalDate dueDate;
}