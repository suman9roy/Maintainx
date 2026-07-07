package com.maintainx.complaint_service.dto;

import com.maintainx.complaint_service.enums.ComplaintStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ComplaintStatusUpdateRequest {

    @NotNull(message = "Status is required")
    private ComplaintStatus status;
}