package com.maintainx.payment_service.client;

import com.maintainx.payment_service.dto.ResidentResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;

@FeignClient(name = "resident-service")
public interface ResidentClient {
    // Define methods to interact with the resident-service endpoints
    //add method to get all resident with flat number
    @GetMapping("/residents/byFlatNumber/{flatNumber}")
    List<ResidentResponse> getResidentsByFlatNumber(@PathVariable String flatNumber,
                                                    @RequestHeader("X-Apartment-Id") String apartmentId);

}
