package com.maintainx.payment_service.client;

import com.maintainx.payment_service.dto.MaintenanceBillResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import com.maintainx.payment_service.dto.MarkBillPaidRequest;

import java.util.UUID;

@FeignClient(name = "maintenance-service")
public interface MaintenanceClient {

    /**
     * userId and role are the ORIGINAL caller's identity — extracted
     * from the gateway-verified JWT in PaymentController, then threaded
     * through PaymentService.createOrder() into this call. This lets
     * maintenance-service apply the exact same ownership check it would
     * for a direct resident call, closing the IDOR gap where a resident
     * could otherwise pass any bill UUID and pay another flat's bill.
     */
    @GetMapping("/maintenance/bill/{id}")
    MaintenanceBillResponse getBill(
            @PathVariable UUID id,
            @RequestHeader("X-User-Id")   String userId,
            @RequestHeader("X-User-Role") String role);

    /**
     * No identity headers needed here — this is only called internally
     * by payment-service right after a Razorpay signature is verified,
     * so the payment is already confirmed legitimate at that point.
     */
    @PutMapping("/maintenance/mark-paid/{id}")
    void markBillAsPaid(
            @PathVariable UUID id,
            @RequestBody MarkBillPaidRequest request,
            @RequestHeader("X-User-Id") String adminId);
}