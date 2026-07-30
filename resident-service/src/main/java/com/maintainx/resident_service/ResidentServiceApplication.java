package com.maintainx.resident_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class ResidentServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ResidentServiceApplication.class, args);
	}

}
