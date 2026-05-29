package com.smartcrm.customer.service;

import com.smartcrm.customer.dto.CustomerScoringRequest;
import com.smartcrm.customer.dto.CustomerScoringResponse;
import com.smartcrm.customer.entity.CustomerScore;
import com.smartcrm.customer.mapper.CustomerScoreMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Customer scoring service with AI-powered evaluation
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerScoringService {

    private final CustomerScoreMapper customerScoreMapper;
    private final AiScoringService aiScoringService;

    /**
     * Calculate customer score based on various metrics
     */
    public CustomerScoringResponse calculateScore(CustomerScoringRequest request) {
        log.info("Calculating score for customer: {}", request.getCustomerId());

        // Use AI to analyze customer data
        CustomerScoringResponse response = aiScoringService.analyzeCustomer(request);

        // Save score to database
        saveCustomerScore(request, response);

        return response;
    }

    /**
     * Get existing score for customer
     */
    public CustomerScoringResponse getScore(Long customerId) {
        CustomerScore score = customerScoreMapper.selectOne(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<CustomerScore>()
                .eq(CustomerScore::getCustomerId, customerId)
        );

        if (score == null) {
            return null;
        }

        return mapToResponse(score);
    }

    private void saveCustomerScore(CustomerScoringRequest request, CustomerScoringResponse response) {
        CustomerScore score = new CustomerScore();
        score.setCustomerId(request.getCustomerId());
        score.setCustomerName(request.getCustomerName());
        score.setValueLevel(response.getValueLevel());
        score.setEngagementScore(response.getEngagementScore());
        score.setChurnRisk(response.getChurnRisk());
        score.setTotalRevenue(request.getTotalRevenue());
        score.setTotalOrders(request.getTotalOrders());
        score.setActiveOpportunities(request.getActiveOpportunities());
        score.setAiAnalysis(response.getAiRecommendation());
        score.setLastScoreAt(LocalDateTime.now());
        score.setNextScoreAt(LocalDateTime.now().plusDays(7));

        customerScoreMapper.insert(score);
    }

    private CustomerScoringResponse mapToResponse(CustomerScore score) {
        return CustomerScoringResponse.builder()
                .customerId(score.getCustomerId())
                .customerName(score.getCustomerName())
                .valueLevel(score.getValueLevel())
                .valueLevelLabel(getValueLevelLabel(score.getValueLevel()))
                .engagementScore(score.getEngagementScore())
                .engagementLevel(getEngagementLevel(score.getEngagementScore()))
                .churnRisk(score.getChurnRisk())
                .aiRecommendation(score.getAiAnalysis())
                .build();
    }

    public static String getValueLevelLabel(Integer level) {
        return switch (level) {
            case 1 -> "LV1-潜在客户";
            case 2 -> "LV2-普通客户";
            case 3 -> "LV3-价值客户";
            case 4 -> "LV4-重要客户";
            case 5 -> "LV5-战略客户";
            default -> "未知";
        };
    }

    public static String getEngagementLevel(Integer score) {
        if (score >= 80) return "高";
        if (score >= 60) return "中";
        if (score >= 40) return "低";
        return "极低";
    }
}