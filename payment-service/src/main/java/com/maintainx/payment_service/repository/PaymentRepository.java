package com.maintainx.payment_service.repository;



import com.maintainx.payment_service.entity.Payment;
import com.maintainx.payment_service.enums.BillSyncStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository
        extends JpaRepository<Payment, Long> {
    Optional<Payment> findByRazorpayOrderId(String razorpayOrderId);

    // Used by BillSyncRetryScheduler to find payments needing a retry
    List<Payment> findByBillSyncStatus(BillSyncStatus billSyncStatus);
}