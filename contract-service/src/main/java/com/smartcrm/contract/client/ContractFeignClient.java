package com.smartcrm.contract.client;

import com.smartcrm.common.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Feign client for Contract Service via Nacos service discovery.
 * Enables other services to access contract data.
 */
@FeignClient(name = "contract-service")
public interface ContractFeignClient {

    @GetMapping("/api/contracts")
    ApiResponse<List<Map<String, Object>>> getAllContracts();

    @GetMapping("/api/contracts/{id}")
    ApiResponse<Map<String, Object>> getContractById(@PathVariable Long id);

    @GetMapping("/api/contracts/status/{status}")
    ApiResponse<List<Map<String, Object>>> getContractsByStatus(@PathVariable String status);

    @GetMapping("/api/contracts/owner/{ownerId}")
    ApiResponse<List<Map<String, Object>>> getContractsByOwner(@PathVariable Long ownerId);

    @GetMapping("/api/contracts/account/{accountId}")
    ApiResponse<List<Map<String, Object>>> getContractsByAccount(@PathVariable Long accountId);

    @GetMapping("/api/contracts/opportunity/{opportunityId}")
    ApiResponse<List<Map<String, Object>>> getContractsByOpportunity(@PathVariable Long opportunityId);

    @GetMapping("/api/contracts/stats/total-value")
    ApiResponse<BigDecimal> getTotalContractValue();

    @GetMapping("/api/contracts/stats/outstanding-amount")
    ApiResponse<BigDecimal> getTotalOutstandingAmount();

    @GetMapping("/api/contracts/stats/count-by-status")
    ApiResponse<Map<String, Long>> countByStatus();

    @GetMapping("/api/contracts/expiring/{daysThreshold}")
    ApiResponse<List<Map<String, Object>>> getExpiringContracts(@PathVariable int daysThreshold);

    @GetMapping("/api/contracts/overdue")
    ApiResponse<List<Map<String, Object>>> getOverdueContracts();
}
