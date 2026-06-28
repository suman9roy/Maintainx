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

    /**
     * Links to Users.id in auth-service.
     * NOT unique — one user can own/rent multiple flats.
     */
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    private String fullName;

    @Column(unique = true)
    private String email;

    private String phoneNumber;
    private String flatNumber;
    private String blockName;
    private Integer floorNumber;

    @Enumerated(EnumType.STRING)
    private ResidentType residentType;      // OWNER | TENANT | FAMILY_MEMBER

    /** Reference back to the join request that created this record */
    @Column(name = "join_request_id")
    private Long joinRequestId;
}