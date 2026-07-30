package com.maintainx.resident_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

@FeignClient(name = "auth-service")
public interface AuthApartmentClient {

    /** Returns {"exists": true/false, "active": true/false} */
    @GetMapping("/apartments/public/{id}/active")
    Map<String, Boolean> checkActive(@PathVariable("id") String apartmentId);
}
