package com.maintainx.payment_service.config;


import com.razorpay.RazorpayClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
@Slf4j
@Configuration
public class RazorpayConfig {

    @Value("${razorpay.key}")
    private String key;

    @Value("${razorpay.secret}")
    private String secret;

    @Bean
    public RazorpayClient razorpayClient()
            throws Exception {
        log.info("Initializing RazorpayClient with key: {} and secret: {}", key, secret);

        return new RazorpayClient(
                key,
                secret
        );
    }
}