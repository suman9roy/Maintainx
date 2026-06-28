package com.maintainx.resident_service.dto;




import lombok.Data;
import java.util.UUID;
@Data
public class ResidentRequest {

    /**
     * The UUID returned from POST /auth/register.
     * Admin registers the user first, then passes
     * that UUID here to link the profile to the account.
     */
    private UUID userId;

    private String fullName;
    private String email;
    private String phoneNumber;
    private String flatNumber;
    private String blockName;
    private Integer floorNumber;
    private String residentType;
}