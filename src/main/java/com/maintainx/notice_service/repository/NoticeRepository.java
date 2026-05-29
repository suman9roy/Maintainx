package com.maintainx.notice_service.repository;

import com.maintainx.notice_service.entity.Notice;
import com.maintainx.notice_service.enums.NoticeType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NoticeRepository extends JpaRepository<Notice,Long> {

    List<Notice> findByType(NoticeType type);
}
