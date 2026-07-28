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

    @GetMapping
    public List<Notice> getAllNotices(@RequestHeader("X-Apartment-Id") String apartmentId) {
        return service.getAllNotices(UUID.fromString(apartmentId));
    }

    @GetMapping("/type/{type}")
    public List<Notice> getByType(@PathVariable NoticeType type,
                                  @RequestHeader("X-Apartment-Id") String apartmentId) {
        return service.getByType(type,
                UUID.fromString(apartmentId));
    }
}