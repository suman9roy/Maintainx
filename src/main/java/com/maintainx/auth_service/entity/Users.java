package com.maintainx.auth_service.entity;

import jakarta.persistence.*;
import lombok.*;


import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Users {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String role;                    // "ADMIN" | "RESIDENT"

    /**
     * Aadhaar number — admin cross-verifies this against the
     * uploaded flat deed / rental agreement PDF.
     *
     * PRODUCTION NOTE: encrypt this column using an
     * AttributeConverter + AES-256. Aadhaar is PII regulated
     * by UIDAI and must never be stored in plain text.
     */
    @Column(name = "aadhar_number", unique = true)
    private String aadharNumber;
}