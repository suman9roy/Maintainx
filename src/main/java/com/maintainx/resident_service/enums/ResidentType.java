package com.maintainx.resident_service.enums;


public enum ResidentType {
    OWNER,          // owns the flat, has flat deed
    TENANT,         // renting, has rental agreement
    FAMILY_MEMBER   // family of owner/tenant, no separate document required
}