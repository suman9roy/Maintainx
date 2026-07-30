package com.maintainx.auth_service.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Mirrors com.maintainx.resident_service.kafka.JoinRequestStatusEvent
 * (and notification-service's copy of the same). Must have identical
 * field names for Jackson deserialization to work.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JoinRequestStatusEvent {

    private String userId;
    private String apartmentId;
    private String residentEmail;
    private String fullName;
    private String flatNumber;
    private String status;           // "APPROVED" | "REJECTED"
    private String rejectionReason;  // null when APPROVED
}