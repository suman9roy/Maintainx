package com.maintainx.payment_service.service;


import com.maintainx.payment_service.client.MaintenanceClient;
import com.maintainx.payment_service.dto.*;
import com.maintainx.payment_service.entity.Payment;
import com.maintainx.payment_service.enums.PaymentStatus;
import com.maintainx.payment_service.repository.PaymentRepository;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.Utils;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PaymentService {
    @Value("${razorpay.secret}")
    private String secret;
    private final MaintenanceClient maintenanceClient;
    private final RazorpayClient razorpayClient;
    private final PaymentRepository repository;
    public RazorpayOrderResponse createOrder(
            CreateOrderRequest request)
            throws Exception {

        MaintenanceBillResponse bill =
                maintenanceClient.getBill(
                        request.getMaintenanceBillId()
                );

        if (bill.getPaymentStatus()
                .equals("PAID")) {

            throw new RuntimeException(
                    "Bill already paid"
            );
        }

        JSONObject orderRequest =
                new JSONObject();

        orderRequest.put(
                "amount",
                bill.getAmount() * 100
        );

        orderRequest.put("currency", "INR");

        orderRequest.put(
                "receipt",
                "txn_" + System.currentTimeMillis()
        );

        Order order =
                razorpayClient.orders
                        .create(orderRequest);

        Payment payment =
                Payment.builder()
                        .maintenanceBillId(
                                bill.getId()
                        )
                        .flatNumber(
                                bill.getFlatNumber()
                        )
                        .amount(
                                bill.getAmount()
                        )
                        .razorpayOrderId(
                                order.get("id")
                        )
                        .paymentStatus(
                                PaymentStatus.CREATED
                        )
                        .paymentDate(
                                LocalDateTime.now()
                        )
                        .build();

        repository.save(payment);

        return RazorpayOrderResponse.builder()
                .orderId(order.get("id"))
                .amount(bill.getAmount())
                .currency("INR")
                .build();
    }
    public String verifyPayment(
            PaymentVerificationRequest request)
            throws Exception {

        String generatedSignature =
                Utils.getHash(
                        request.getRazorpayOrderId()
                                + "|"
                                + request.getRazorpayPaymentId(),
                        secret
                );

        if (!generatedSignature.equals(
                request.getRazorpaySignature())) {

            throw new RuntimeException(
                    "Invalid Payment Signature"
            );
        }

        Payment payment =
                repository.findByRazorpayOrderId(
                        request.getRazorpayOrderId()
                ).orElseThrow();

        payment.setRazorpayPaymentId(
                request.getRazorpayPaymentId()
        );

        payment.setRazorpaySignature(
                request.getRazorpaySignature()
        );

        payment.setPaymentStatus(
                PaymentStatus.SUCCESS
        );

        repository.save(payment);

        maintenanceClient.markBillAsPaid(
                payment.getMaintenanceBillId()
        );

        return "Payment Verified Successfully";
    }
}