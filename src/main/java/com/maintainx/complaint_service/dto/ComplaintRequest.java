package com.maintainx.complaint_service.dto;


import com.maintainx.complaint_service.enums.ComplaintCategory;
import lombok.Data;

@Data
public class ComplaintRequest {

    private String residentEmail;

    private String flatNumber;

    private String title;

    private String description;

    private ComplaintCategory category;
}
