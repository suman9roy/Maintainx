package com.maintainx.complaint_service.entity;


import com.maintainx.complaint_service.enums.ComplaintCategory;
import com.maintainx.complaint_service.enums.ComplaintStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "complaints")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Complaint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "apartment_id", nullable = false)
    private UUID apartmentId;

    private String residentEmail;

    private String flatNumber;

    private String title;

    private String description;

    @Enumerated(EnumType.STRING)
    private ComplaintCategory category;

    @Enumerated(EnumType.STRING)
    private ComplaintStatus status;

    private LocalDateTime createdAt;
}