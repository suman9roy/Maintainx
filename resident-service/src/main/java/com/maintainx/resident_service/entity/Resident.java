package com.maintainx.resident_service.entity;


import com.maintainx.resident_service.enums.ResidentType;
import jakarta.persistence.*;
import lombok.*;


import java.util.UUID;


@Entity
@Table(name = "residents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Resident {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    /**
     * Which apartment this resident record belongs to. Set from the
     * approved join request's apartmentId — never trust a client-supplied
     * value for this field.
     */
    @Column(name = "apartment_id", nullable = false)
    private UUID apartmentId;

    private String fullName;

    @Column(unique = true)
    private String email;

    private String phoneNumber;
    private String flatNumber;
    private String blockName;
    private Integer floorNumber;

    @Enumerated(EnumType.STRING)
    private ResidentType residentType;

    @Column(name = "join_request_id")
    private Long joinRequestId;
}
