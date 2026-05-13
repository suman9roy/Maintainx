package com.maintainx.payment_service.service;


import com.maintainx.payment_service.dto.PaymentRequest;
import com.maintainx.payment_service.entity.Payment;
import com.maintainx.payment_service.repository.PaymentRepository;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final RazorpayClient razorpayClient;

    private final PaymentRepository repository;

    public String createOrder(
            PaymentRequest request)
            throws Exception {

        JSONObject orderRequest =
                new JSONObject();

        orderRequest.put(
                "amount",
                request.getAmount() * 100
        );

        orderRequest.put(
                "currency",
                "INR"
        );

        orderRequest.put(
                "receipt",
                "txn_" + System.currentTimeMillis()
        );

        Order order =
                razorpayClient.orders
                        .create(orderRequest);

        Payment payment =
                Payment.builder()
                        .flatNumber(
                                request.getFlatNumber()
                        )
                        .amount(request.getAmount())
                        .razorpayOrderId(
                                order.get("id")
                        )
                        .paymentStatus("CREATED")
                        .paymentDate(
                                LocalDateTime.now()
                        )
                        .build();

        repository.save(payment);

        return order.toString();
    }
}