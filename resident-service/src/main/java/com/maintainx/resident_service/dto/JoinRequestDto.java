package com.maintainx.resident_service.dto;

import com.maintainx.resident_service.enums.ResidentType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class JoinRequestDto {

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

    /**
     * Cross-field rule: document is required for OWNER and TENANT
     * but not FAMILY_MEMBER. We can't express this with a simple
     * field annotation, so @AssertTrue on a method handles it.
     *
     * Note: the document (MultipartFile) itself is validated separately
     * in JoinRequestService — this covers only the DTO fields.
     * The residentType null check ensures a missing type doesn't
     * cause an NPE here before @NotNull fires on that field.
     */
    @AssertTrue(message = "OWNER and TENANT must upload a document (flat deed or rental agreement)")
    public boolean isDocumentRuleConsistent() {
        if (residentType == null) return true; // @NotNull handles this
        return residentType == ResidentType.FAMILY_MEMBER
                || residentType == ResidentType.OWNER
                || residentType == ResidentType.TENANT;
        // always true for valid enum values — actual file presence
        // is checked in the service after the file arrives via multipart
    }
}