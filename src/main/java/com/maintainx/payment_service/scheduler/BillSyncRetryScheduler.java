package com.maintainx.payment_service.scheduler;

import com.maintainx.payment_service.client.MaintenanceClient;
import com.maintainx.payment_service.entity.Payment;
import com.maintainx.payment_service.enums.BillSyncStatus;
import com.maintainx.payment_service.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class BillSyncRetryScheduler {

    private final PaymentRepository repository;
    private final MaintenanceClient maintenanceClient;

    // After this many failed attempts, stop retrying automatically
    private static final int MAX_RETRY_ATTEMPTS = 5;

    /**
     * Runs every 5 minutes by default (configurable via
     * bill-sync.retry-interval-ms in application.yml).
     *
     * Finds every payment stuck at PENDING_RETRY and attempts
     * the maintenance-service Feign call again. This closes the
     * loop so an admin doesn't need to notice and fix these by hand
     * for transient failures (service restart, brief network blip, etc).
     */
    @Scheduled(fixedDelayString = "${bill-sync.retry-interval-ms:300000}")
    public void retryPendingBillSyncs() {

        List<Payment> pending = repository.findByBillSyncStatus(BillSyncStatus.PENDING_RETRY);

        if (pending.isEmpty()) {
            return; // nothing to do — skip noisy logging every 5 minutes
        }

        log.info("Bill sync retry job — found {} payment(s) pending retry", pending.size());

        // Each payment retried independently — one failure in the batch
        // must not affect the others.
        for (Payment payment : pending) {
            retrySingle(payment.getId());
        }
    }

    /**
     * Retries a single payment's bill sync in its own transaction.
     * Re-fetches the row fresh in case it was already resolved
     * by a manual admin action between the query above and now.
     */
    @Transactional
    public void retrySingle(Long paymentId) {

        Payment payment = repository.findById(paymentId).orElse(null);

        if (payment == null || payment.getBillSyncStatus() != BillSyncStatus.PENDING_RETRY) {
            return; // already resolved — nothing to do
        }

        try {
            maintenanceClient.markBillAsPaid(payment.getMaintenanceBillId());
            payment.setBillSyncStatus(BillSyncStatus.SYNCED);

            log.info("Bill sync retry SUCCEEDED — paymentId={}, billId={}",
                    payment.getId(), payment.getMaintenanceBillId());

        } catch (Exception e) {

            int attempts = payment.getRetryCount() + 1;
            payment.setRetryCount(attempts);

            if (attempts >= MAX_RETRY_ATTEMPTS) {
                payment.setBillSyncStatus(BillSyncStatus.SYNC_FAILED);
                log.error("Bill sync retry FAILED PERMANENTLY — paymentId={}, billId={} " +
                                "after {} attempts. Manual intervention required: " +
                                "call PUT /maintenance/mark-paid/{} directly.",
                        payment.getId(), payment.getMaintenanceBillId(), attempts,
                        payment.getMaintenanceBillId());
            } else {
                log.warn("Bill sync retry attempt {} of {} failed — paymentId={}, billId={}: {}",
                        attempts, MAX_RETRY_ATTEMPTS, payment.getId(),
                        payment.getMaintenanceBillId(), e.getMessage());
            }
        }

        repository.save(payment);
    }
}