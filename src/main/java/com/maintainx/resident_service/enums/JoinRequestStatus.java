package com.maintainx.resident_service.enums;


public enum JoinRequestStatus {
    PENDING,    // submitted, waiting for admin review
    APPROVED,   // admin approved → Resident record created
    REJECTED,// admin rejected → reason stored
    ALL
}
