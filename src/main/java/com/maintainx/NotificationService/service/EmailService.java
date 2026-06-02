package com.maintainx.NotificationService.service;


import lombok.RequiredArgsConstructor;

import org.springframework.mail.SimpleMailMessage;

import org.springframework.mail.javamail.JavaMailSender;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendPaymentSuccessMail(

            String to,
            Double amount,
            String flatNumber) {

        SimpleMailMessage message =
                new SimpleMailMessage();

        message.setTo(to);

        message.setSubject(
                "MaintainX Payment Successful"
        );

        message.setText(

                "Your maintenance payment of ₹"
                        + amount
                        + " for flat "
                        + flatNumber
                        + " was successful."
        );

        mailSender.send(message);
        System.out.println("Your mail send successfully");
    }
}