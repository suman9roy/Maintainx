package com.maintainx.notice_service.repository;

import com.maintainx.notice_service.entity.Notice;
import com.maintainx.notice_service.enums.NoticeType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface NoticeRepository extends JpaRepository<Notice,Long> {



    List<Notice> findAllByApartmentId(UUID apartmentId);

    List<Notice> findByTypeAndApartmentId(NoticeType type, UUID apartmentId);
}
