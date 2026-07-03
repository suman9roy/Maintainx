package com.maintainx.complaint_service.client;

import com.maintainx.complaint_service.dto.ResidentSummary;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;

@FeignClient(name = "resident-service")
public interface ResidentClient {
    @GetMapping("/residents/byUserId")
    List<ResidentSummary> getResidentsForUser(
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Role") String role);
}
