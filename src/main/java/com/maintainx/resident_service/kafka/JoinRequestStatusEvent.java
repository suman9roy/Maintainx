package com.maintainx.resident_service.kafka;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JoinRequestStatusEvent {

    private String userId;          // UUID as string
    private String residentEmail;   // to send notification email
    private String fullName;
    private String flatNumber;
    private String status;          // "APPROVED" | "REJECTED"
    private String rejectionReason; // null when APPROVED
}