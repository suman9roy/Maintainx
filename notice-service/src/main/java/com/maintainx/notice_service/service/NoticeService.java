package com.maintainx.notice_service.service;


import com.maintainx.notice_service.dto.NoticeRequest;
import com.maintainx.notice_service.entity.Notice;
import com.maintainx.notice_service.enums.NoticeType;
import com.maintainx.notice_service.repository.NoticeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NoticeService {

    private final NoticeRepository repository;

    public Notice createNotice(
            NoticeRequest request, UUID apartmentId) {
        // Validate that the apartmentId in the request header matches the apartmentId in the request body
        if (!apartmentId.toString().equals(request.getApartmentId())) {
            throw new IllegalArgumentException("Apartment ID in request header does not match the apartment ID in the request body");
        }

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
                        ).apartmentId(apartmentId)
                        .build();

        return repository.save(notice);
    }

    public List<Notice> getAllNotices(UUID apartmentId) {

        return repository.findAllByApartmentId(apartmentId);
    }

    public List<Notice> getByType(
            NoticeType type, UUID apartmentId) {

        return repository.findByTypeAndApartmentId(type, apartmentId);
    }
}