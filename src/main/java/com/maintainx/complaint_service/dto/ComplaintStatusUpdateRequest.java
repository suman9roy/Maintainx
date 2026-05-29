package com.maintainx.complaint_service.dto;



import com.maintainx.complaint_service.enums.ComplaintStatus;
import lombok.Data;

@Data
public class ComplaintStatusUpdateRequest {

    private ComplaintStatus status;
}