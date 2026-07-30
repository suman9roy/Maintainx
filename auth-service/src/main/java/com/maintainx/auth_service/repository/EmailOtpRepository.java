package com.maintainx.auth_service.repository;

import com.maintainx.auth_service.entity.EmailOtp;
import com.maintainx.auth_service.enums.OtpPurpose;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmailOtpRepository extends JpaRepository<EmailOtp, Long> {

    Optional<EmailOtp> findTopByEmailAndPurposeOrderByCreatedAtDesc(
            String email, OtpPurpose purpose);
}
