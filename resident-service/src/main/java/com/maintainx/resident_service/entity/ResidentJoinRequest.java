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

    /**
     * Original filename shown to admin (e.g. "flat-deed-B204.pdf").
     * Null for FAMILY_MEMBER — no document required.
     */
    @Column(name = "document_name")
    private String documentName;

    /**
     * Path on disk where the PDF is stored.
     * Format: {upload-dir}/{userId}/{uuid}-{originalFilename}
     *
     * PRODUCTION: replace with S3 object key.
     */
    @Column(name = "document_path")
    private String documentPath;

    // ── Status ────────────────────────────────────────────────────────────────

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private JoinRequestStatus status;

    @Column(name = "rejection_reason")
    private String rejectionReason;         // set on REJECTED

    @Column(name = "requested_at", nullable = false)
    private LocalDateTime requestedAt;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;       // set when admin acts
}