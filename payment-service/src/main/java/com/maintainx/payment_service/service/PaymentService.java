package com.maintainx.payment_service.service;

import com.maintainx.payment_service.client.MaintenanceClient;
import com.maintainx.payment_service.dto.*;
import com.maintainx.payment_service.entity.Payment;
import com.maintainx.payment_service.enums.BillSyncStatus;
import com.maintainx.payment_service.enums.PaymentStatus;
import com.maintainx.payment_service.event.PaymentSuccessEvent;
import com.maintainx.payment_service.event.PaymentVerifiedEvent;
import com.maintainx.payment_service.exception.InvalidRequestException;
import com.maintainx.payment_service.exception.ResourceNotFoundException;
import com.maintainx.payment_service.repository.PaymentRepository;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.Utils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.maintainx.payment_service.dto.MarkBillPaidRequest;
import com.maintainx.payment_service.dto.PaymentMode;
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    @Value("${razorpay.secret}")
    private String keySecret;
    @Value("${razorpay.key}")
    private String keyId;
    @Value("${service.system-admin-id:00000000-0000-0000-0000-000000000000}")
    private String systemAdminId;
    private final MaintenanceClient maintenanceClient;

    private final PaymentRepository repository;
    private final ApplicationEventPublisher eventPublisher;
    private final RazorpayClient client;
    public RazorpayOrderResponse createOrder(
            CreateOrderRequest request, String userId, String role) throws Exception {



        MaintenanceBillResponse bill =
                maintenanceClient.getBill(request.getMaintenanceBillId(), userId, role);
        log.info("Retrieved bill for user: {} and bill ID: {}", userId, bill.getId());
        if ("PAID".equals(bill.getPaymentStatus())) {
            // Was: RuntimeException("Bill already paid") → 500
            // Now: InvalidRequestException → 400 with a clear message
            throw new InvalidRequestException(
                    "Bill " + request.getMaintenanceBillId() + " is already paid"
            );
        }
        log.info("Bill retrieved successfully for user: {} and bill ID: {} and amount: {}", userId, bill.getId(), bill.getAmount());
        JSONObject orderRequest = new JSONObject();
        orderRequest.put("amount",
                BigDecimal.valueOf(
                        bill.getAmount().multiply(BigDecimal.valueOf(100)).longValue()));// Razorpay expects amount in paise
        orderRequest.put("currency", "INR");
        orderRequest.put("receipt", "txn_" + System.currentTimeMillis());
        log.info("orderRequest JSON: {}", orderRequest.toString());
        Order order = client.orders.create(orderRequest);
        log.info("Razorpay order created: {}", order.toString());
        Payment payment = Payment.builder()
                .maintenanceBillId(bill.getId())
                .flatNumber(bill.getFlatNumber())
                .amount(bill.getAmount())
                .razorpayOrderId(order.get("id"))
                .paymentStatus(PaymentStatus.CREATED)
                .paymentDate(LocalDateTime.now())
                .build();

        repository.save(payment);

        log.info("Razorpay order created: orderId={}, billId={}, flat={}",
                order.get("id"), bill.getId(), bill.getFlatNumber());

        return RazorpayOrderResponse.builder()
                .orderId(order.get("id"))
                .amount(bill.getAmount())
                .currency("INR")
                .build();
    }

    @Transactional
    public String verifyPayment(PaymentVerificationRequest request) throws Exception {

        String generatedSignature = Utils.getHash(
                request.getRazorpayOrderId() + "|" + request.getRazorpayPaymentId(),
                keySecret
        );

        if (!generatedSignature.equals(request.getRazorpaySignature())) {
            // Was: RuntimeException("Invalid Payment Signature") → 500
            // Now: InvalidRequestException → 400
            // Signature mismatch is a bad client request, not a server error
            throw new InvalidRequestException(
                    "Payment signature verification failed — possible tampered request"
            );
        }

        Payment payment = repository.findByRazorpayOrderId(request.getRazorpayOrderId())
                // Was: RuntimeException("Payment not found") → 500
                // Now: ResourceNotFoundException → 404
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No payment record found for order ID: " + request.getRazorpayOrderId()
                ));

        payment.setRazorpayPaymentId(request.getRazorpayPaymentId());
        payment.setRazorpaySignature(request.getRazorpaySignature());
        payment.setPaymentStatus(PaymentStatus.SUCCESS);

        try {
            MarkBillPaidRequest markReq = new MarkBillPaidRequest();
            markReq.setPaymentMode(PaymentMode.ONLINE);
            markReq.setRemarks("Paid via Razorpay — payment id: " + payment.getRazorpayPaymentId());

            maintenanceClient.markBillAsPaid(
                    payment.getMaintenanceBillId(),
                    markReq,
                    systemAdminId
            );

            payment.setBillSyncStatus(BillSyncStatus.SYNCED);

        } catch (Exception e) {
            log.error("Failed to mark bill {} as paid — payment {} is SUCCESS but needs retry: {}",
                    payment.getMaintenanceBillId(), payment.getId(), e.getMessage());
            payment.setBillSyncStatus(BillSyncStatus.PENDING_RETRY);
        }
        repository.save(payment);

        eventPublisher.publishEvent(new PaymentVerifiedEvent(
                PaymentSuccessEvent.builder()
                        .maintenanceBillId(payment.getMaintenanceBillId())
                        .flatNumber(payment.getFlatNumber())
                        //to-do email id of resident should be fetched from resident service need a client for that
                        .residentEmail("suman99999roy@gmail.com")
                        .amount(payment.getAmount())
                        .paymentId(payment.getRazorpayPaymentId())
                        .build()
        ));

        log.info("Payment verified — paymentId={}, billId={}, syncStatus={}",
                payment.getId(), payment.getMaintenanceBillId(), payment.getBillSyncStatus());

        return payment.getBillSyncStatus() == BillSyncStatus.PENDING_RETRY
                ? "Payment Verified Successfully — bill update is pending and will be retried"
                : "Payment Verified Successfully";
    }
}