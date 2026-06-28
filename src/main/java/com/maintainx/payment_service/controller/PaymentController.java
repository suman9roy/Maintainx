package com.maintainx.payment_service.controller;

import com.maintainx.payment_service.dto.CreateOrderRequest;
import com.maintainx.payment_service.dto.PaymentVerificationRequest;
import com.maintainx.payment_service.dto.RazorpayOrderResponse;
import com.maintainx.payment_service.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService service;

    @PostMapping("/create-order")
    public RazorpayOrderResponse createOrder(
            @Valid @RequestBody CreateOrderRequest request,
            @RequestHeader("X-User-Id")   String userId,
            @RequestHeader("X-User-Role") String role) throws Exception {

        return service.createOrder(request, userId, role);
    }

    // @Valid ensures none of the three Razorpay fields are blank
    // before Utils.getHash() is called — prevents a silent NPE
    // that would otherwise appear as a confusing 500 error
    @PostMapping("/verify")
    public String verifyPayment(
            @Valid @RequestBody PaymentVerificationRequest request) throws Exception {

        return service.verifyPayment(request);
    }
}