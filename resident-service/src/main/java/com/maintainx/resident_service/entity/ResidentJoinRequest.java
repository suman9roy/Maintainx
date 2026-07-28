package com.maintainx.resident_service.entity;


import com.maintainx.resident_service.enums.JoinRequestStatus;
import com.maintainx.resident_service.enums.ResidentType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "resident_join_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResidentJoinRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ── Who is requesting ─────────────────────────────────────────────────────

    /** UUID from JWT — injected by gateway, never from request body */
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    /**
     * Which apartment this request is for. Chosen by the applicant from
     * the public apartment list (auth-service GET /apartments/public) —
     * required because at the time of applying, the user's own JWT/Users
     * record has no apartmentId yet.
     */
    @Column(name = "apartment_id", nullable = false)
    private UUID apartmentId;

    @Column(nullable = false)
    private String fullName;
    @Column(nullable = false,unique = true)
    private String residentEmail;

    @Column(nullable = false)
    private String phoneNumber;

    // ── Which flat ────────────────────────────────────────────────────────────

    @Column(nullable = false)
    private String flatNumber;

    @Column(nullable = false)
    private String blockName;

    @Column(nullable = false)
    private Integer floorNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ResidentType residentType;      // OWNER | TENANT | FAMILY_MEMBER

    // ── Document ─────────────────────────────────────────────────────────────

    @Column(name = "document_name")
    private String documentName;

    @Column(name = "document_path")
    private String documentPath;

    // ── Status ────────────────────────────────────────────────────────────────

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private JoinRequestStatus status;

    @Column(name = "rejection_reason")
    private String rejectionReason;

    @Column(name = "requested_at", nullable = false)
    private LocalDateTime requestedAt;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;
}
