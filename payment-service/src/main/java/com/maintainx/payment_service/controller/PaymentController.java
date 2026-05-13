package com.maintainx.payment_service.controller;

import com.maintainx.payment_service.dto.CreateOrderRequest;
import com.maintainx.payment_service.dto.PaymentVerificationRequest;
import com.maintainx.payment_service.dto.RazorpayOrderResponse;
import com.maintainx.payment_service.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService service;

    @PostMapping("/create-order")
    public RazorpayOrderResponse createOrder(
            @RequestBody CreateOrderRequest request)
            throws Exception {

        return service.createOrder(request);
    }

    @PostMapping("/verify")
    public String verifyPayment(
            @RequestBody
            PaymentVerificationRequest request)
            throws Exception {

        return service.verifyPayment(request);
    }
}