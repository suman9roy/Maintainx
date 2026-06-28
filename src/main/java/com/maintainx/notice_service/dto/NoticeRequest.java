package com.maintainx.notice_service.dto;

import com.maintainx.notice_service.enums.NoticeType;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NoticeRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 150, message = "Title must be under 150 characters")
    private String title;

    @NotBlank(message = "Message is required")
    @Size(max = 2000, message = "Message must be under 2000 characters")
    // 2000 matches the entity's @Column(length = 2000) — this also
    // stops an oversized message from ever reaching the DB and
    // failing with a column-too-long error instead of a clean 400.
    private String message;

    @NotNull(message = "Type is required")
    private NoticeType type;

    private LocalDateTime meetingTime;

    /**
     * Cross-field rule that simple annotations can't express alone:
     * meetingTime is required ONLY when type is MEETING.
     * Jakarta Validation runs any @AssertTrue method automatically —
     * if it returns false, validation fails with the given message.
     */
    @AssertTrue(message = "meetingTime is required when notice type is MEETING")
    public boolean isMeetingTimeValid() {
        if (type == NoticeType.MEETING) {
            return meetingTime != null;
        }
        return true;
    }
}