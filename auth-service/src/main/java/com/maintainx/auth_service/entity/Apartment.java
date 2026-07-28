package com.maintainx.auth_service.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * A tenant in the SaaS sense — one onboarded apartment/society.
 * Every Users row (except SUPER_ADMIN) belongs to exactly one Apartment,
 * and every domain entity in every other service will carry this id too
 * to enforce data isolation between apartments.
 */
@Entity
@Table(name = "apartments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Apartment {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(nullable = false)
    private String name;

    private String address;
    private String city;

    /**
     * Super admin can suspend a tenant (e.g. non-payment) without
     * deleting their data. Services should also check this before
     * allowing logins/writes for that apartment's users — that
     * enforcement comes in a later step once other services are wired up.
     */
    @Builder.Default
    @Column(nullable = false)
    private Boolean active = true;

    @Builder.Default
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
