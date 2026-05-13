package com.maintainx.notice_service.dto;

import com.maintainx.notice_service.enums.NoticeType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NoticeRequest {

    private String title;

    private String message;

    private NoticeType type;

    private LocalDateTime meetingTime;
}
