package com.maintainx.resident_service.entity;


import jakarta.persistence.*;
import lombok.*;

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

    private String fullName;

    @Column(unique = true)
    private String email;

    private String phoneNumber;

    private String flatNumber;

    private String blockName;

    private Integer floorNumber;

    private String residentType;
}
