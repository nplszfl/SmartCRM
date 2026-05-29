package com.smartcrm.customer.controller;

import com.smartcrm.common.dto.ApiResponse;
import com.smartcrm.customer.dto.CustomerScoringRequest;
import com.smartcrm.customer.dto.CustomerScoringResponse;
import com.smartcrm.customer.service.CustomerScoringService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Customer scoring REST controller
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/customer-scores")
public class CustomerScoringController {

    private final CustomerScoringService customerScoringService;

    @PostMapping("/calculate")
    public ApiResponse<CustomerScoringResponse> calculateScore(@RequestBody CustomerScoringRequest request) {
        CustomerScoringResponse response = customerScoringService.calculateScore(request);
        return ApiResponse.success(response);
    }

    @GetMapping("/{customerId}")
    public ApiResponse<CustomerScoringResponse> getScore(@PathVariable Long customerId) {
        CustomerScoringResponse response = customerScoringService.getScore(customerId);
        if (response == null) {
            return ApiResponse.error(404, "Score not found");
        }
        return ApiResponse.success(response);
    }

    @GetMapping("/batch")
    public ApiResponse<List<CustomerScoringResponse>> getBatchScores(@RequestParam List<Long> customerIds) {
        return ApiResponse.success(customerIds.stream()
                .map(customerScoringService::getScore)
                .filter(r -> r != null)
                .toList());
    }

    @GetMapping("/high-risk")
    public ApiResponse<List<CustomerScoringResponse>> getHighRiskCustomers() {
        // This would query customers with HIGH churn risk
        return ApiResponse.success(List.of());
    }

    @GetMapping("/value-level/{level}")
    public ApiResponse<List<CustomerScoringResponse>> getByValueLevel(@PathVariable Integer level) {
        // This would filter by value level
        return ApiResponse.success(List.of());
    }
}