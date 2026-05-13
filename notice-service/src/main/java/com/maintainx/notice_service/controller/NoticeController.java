package com.maintainx.notice_service.controller;

import com.maintainx.notice_service.dto.NoticeRequest;
import com.maintainx.notice_service.entity.Notice;
import com.maintainx.notice_service.enums.NoticeType;
import com.maintainx.notice_service.service.NoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notices")
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeService service;

    @PostMapping
    public Notice createNotice(
            @RequestBody NoticeRequest request,
            @RequestHeader("X-User-Role")
            String role) {

        if (!role.equals("ADMIN")) {

            throw new RuntimeException(
                    "Only ADMIN can create notices"
            );
        }

        return service.createNotice(request);
    }

    @GetMapping
    public List<Notice> getAllNotices() {

        return service.getAllNotices();
    }

    @GetMapping("/type/{type}")
    public List<Notice> getByType(
            @PathVariable NoticeType type) {

        return service.getByType(type);
    }
}
