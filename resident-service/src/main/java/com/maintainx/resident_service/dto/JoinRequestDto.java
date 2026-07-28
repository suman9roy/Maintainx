package com.maintainx.resident_service.dto;

import com.maintainx.resident_service.enums.ResidentType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.UUID;

@Data
public class JoinRequestDto {

    /**
     * Which apartment the applicant is joining — chosen from the public
     * apartment list. Required because at this point the applicant has
     * no apartment association yet.
     */
    @NotNull(message = "Apartment selection is required")
    private UUID apartmentId;

    @NotBlank(message = "Full name is required")
    @Size(max = 100, message = "Full name must be under 100 characters")
    private String fullName;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[6-9]\\d{9}$",
            message = "Phone number must be a valid 10-digit Indian mobile number")
    private String phoneNumber;

    @NotBlank(message = "Resident email is required")
    @Email(message = "Must be a valid email address")
    private String residentEmail;

    @NotBlank(message = "Flat number is required")
    private String flatNumber;

    @NotBlank(message = "Block name is required")
    private String blockName;

    @NotNull(message = "Floor number is required")
    private Integer floorNumber;

    @NotNull(message = "Resident type is required")
    private ResidentType residentType;

    @AssertTrue(message = "OWNER and TENANT must upload a document (flat deed or rental agreement)")
    public boolean isDocumentRuleConsistent() {
        if (residentType == null) return true;
        return residentType == ResidentType.FAMILY_MEMBER
                || residentType == ResidentType.OWNER
                || residentType == ResidentType.TENANT;
    }
}
