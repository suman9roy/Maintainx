package com.maintainx.notice_service.service;


import com.maintainx.notice_service.dto.NoticeRequest;
import com.maintainx.notice_service.entity.Notice;
import com.maintainx.notice_service.enums.NoticeType;
import com.maintainx.notice_service.repository.NoticeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NoticeService {

    private final NoticeRepository repository;

    public Notice createNotice(
            NoticeRequest request) {

        Notice notice =
                Notice.builder()
                        .title(request.getTitle())
                        .message(request.getMessage())
                        .type(request.getType())
                        .meetingTime(
                                request.getMeetingTime()
                        )
                        .createdAt(
                                LocalDateTime.now()
                        )
                        .build();

        return repository.save(notice);
    }

    public List<Notice> getAllNotices() {

        return repository.findAll();
    }

    public List<Notice> getByType(
            NoticeType type) {

        return repository.findByType(type);
    }
}