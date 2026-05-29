package com.maintainx.notice_service.entity;


import com.maintainx.notice_service.enums.NoticeType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "notices")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(length = 2000)
    private String message;

    @Enumerated(EnumType.STRING)
    private NoticeType type;

    private LocalDateTime createdAt;

    private LocalDateTime meetingTime;
}