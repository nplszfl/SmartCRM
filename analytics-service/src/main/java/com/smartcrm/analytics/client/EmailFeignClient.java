package com.smartcrm.analytics.client;

import com.smartcrm.common.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Feign client for Email Service via Nacos service discovery.
 * Enables analytics to fetch email engagement and campaign metrics.
 */
@FeignClient(name = "email-service")
public interface EmailFeignClient {

    @GetMapping("/api/emails/stats/count-by-status")
    ApiResponse<Map<String, Long>> countByStatus();
}
