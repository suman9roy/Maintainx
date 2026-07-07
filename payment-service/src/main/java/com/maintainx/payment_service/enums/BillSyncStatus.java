package com.maintainx.payment_service.enums;

/**
 * Tracks whether the maintenance-service bill was successfully
 * marked as PAID after a successful Razorpay payment.
 *
 * SYNCED        → markBillAsPaid() succeeded, everything is consistent
 * PENDING_RETRY → markBillAsPaid() failed — payment IS successful
 *                 (money already charged), bill sync will be retried
 *                 automatically by BillSyncRetryScheduler
 * SYNC_FAILED   → retried MAX_RETRY_ATTEMPTS times and still failing.
 *                 Automatic retries stop here — needs manual admin
 *                 intervention (e.g. call mark-paid directly, or
 *                 investigate why maintenance-service is unreachable).
 */
public enum BillSyncStatus {
    SYNCED,
    PENDING_RETRY,
    SYNC_FAILED
}