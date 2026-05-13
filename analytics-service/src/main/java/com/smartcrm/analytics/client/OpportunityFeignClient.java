package com.smartcrm.analytics.client;

import com.smartcrm.common.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Feign client for Opportunity Service via Nacos service discovery.
 * Enables analytics to aggregate opportunity and pipeline data.
 */
@FeignClient(name = "opportunity-service")
public interface OpportunityFeignClient {

    @GetMapping("/api/opportunities")
    ApiResponse<List<Map<String, Object>>> getAllOpportunities();

    @GetMapping("/api/opportunities/stage/{stage}")
    ApiResponse<List<Map<String, Object>>> getOpportunitiesByStage(@PathVariable String stage);

    @GetMapping("/api/opportunities/owner/{ownerId}")
    ApiResponse<List<Map<String, Object>>> getOpportunitiesByOwner(@PathVariable Long ownerId);

    @GetMapping("/api/opportunities/stats/pipeline-value")
    ApiResponse<BigDecimal> getTotalPipelineValue();

    @GetMapping("/api/opportunities/stats/weighted-pipeline-value")
    ApiResponse<BigDecimal> getWeightedPipelineValue();

    @GetMapping("/api/opportunities/stats/count-by-stage")
    ApiResponse<Map<String, Long>> countByStage();
}
