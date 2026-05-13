package com.smartcrm.analytics.client;

import com.smartcrm.common.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Feign client for Lead Service via Nacos service discovery.
 * Enables analytics to aggregate lead data across the CRM platform.
 */
@FeignClient(name = "lead-service")
public interface LeadFeignClient {

    @GetMapping("/api/leads")
    ApiResponse<List<Map<String, Object>>> getAllLeads();

    @GetMapping("/api/leads/status/{status}")
    ApiResponse<List<Map<String, Object>>> getLeadsByStatus(@PathVariable String status);

    @GetMapping("/api/leads/owner/{ownerId}")
    ApiResponse<List<Map<String, Object>>> getLeadsByOwner(@PathVariable Long ownerId);

    @GetMapping("/api/leads/hot")
    ApiResponse<List<Map<String, Object>>> getHotLeads();

    @GetMapping("/api/leads/stats/count-by-status")
    ApiResponse<Map<String, Long>> countByStatus();

    @GetMapping("/api/leads/stats/hot-count")
    ApiResponse<Long> countHotLeads();
}
