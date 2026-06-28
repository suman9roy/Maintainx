package com.maintainx.notice_service.controller;

import com.maintainx.notice_service.dto.NoticeRequest;
import com.maintainx.notice_service.entity.Notice;
import com.maintainx.notice_service.enums.NoticeType;
import com.maintainx.notice_service.service.NoticeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notices")
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeService service;

    @PostMapping
    public Notice createNotice(@Valid @RequestBody NoticeRequest request) {
        return service.createNotice(request);
    }

    @GetMapping
    public List<Notice> getAllNotices() {
        return service.getAllNotices();
    }

    @GetMapping("/type/{type}")
    public List<Notice> getByType(@PathVariable NoticeType type) {
        return service.getByType(type);
    }
}