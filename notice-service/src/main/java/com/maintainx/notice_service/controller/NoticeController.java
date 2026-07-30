package com.maintainx.notice_service.controller;

import com.maintainx.notice_service.dto.NoticeRequest;
import com.maintainx.notice_service.entity.Notice;
import com.maintainx.notice_service.enums.NoticeType;
import com.maintainx.notice_service.service.NoticeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/notices")
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeService service;

    @PostMapping
    public Notice createNotice(@Valid @RequestBody NoticeRequest request,
                               @RequestHeader("X-Apartment-Id") String apartmentId) {
        return service.createNotice(request,
                UUID.fromString(apartmentId));
    }

    /**
     * X-Apartment-Id is intentionally OPTIONAL here (unlike POST, which
     * only admins hit and which always have an apartment). A resident
     * whose join request is still pending has no apartmentId yet — the
     * gateway correctly omits the header in that case rather than
     * sending a bogus value — and calling this endpoint from their
     * dashboard is a normal, expected situation, not an error. They
     * simply have no notices to see yet.
     */
    @GetMapping
    public List<Notice> getAllNotices(
            @RequestHeader(value = "X-Apartment-Id", required = false) String apartmentId) {
        if (apartmentId == null) {
            return List.of();
        }
        return service.getAllNotices(UUID.fromString(apartmentId));
    }

    @GetMapping("/type/{type}")
    public List<Notice> getByType(@PathVariable NoticeType type,
                                  @RequestHeader(value = "X-Apartment-Id", required = false) String apartmentId) {
        if (apartmentId == null) {
            return List.of();
        }
        return service.getByType(type, UUID.fromString(apartmentId));
    }
}