package com.smartcrm.customer.service;

import com.smartcrm.customer.dto.CustomerScoringRequest;
import com.smartcrm.customer.dto.CustomerScoringResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * AI-powered customer scoring service (mock implementation for demo)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiScoringService {

    /**
     * Analyze customer data using AI (fallback to rule-based scoring)
     */
    public CustomerScoringResponse analyzeCustomer(CustomerScoringRequest request) {
        log.info("Analyzing customer: {} with rule-based scoring", request.getCustomerId());

        try {
            // Use rule-based scoring as fallback
            return calculateFallbackScore(request);
        } catch (Exception e) {
            log.error("Error analyzing customer: {}", e.getMessage());
            return createDefaultResponse(request);
        }
    }

    private CustomerScoringResponse calculateFallbackScore(CustomerScoringRequest request) {
        // Calculate value level based on revenue
        int valueLevel = calculateValueLevel(request);
        
        // Calculate engagement score
        int engagementScore = calculateEngagementScore(request);
        
        // Calculate churn risk
        String churnRisk = calculateChurnRisk(request, engagementScore);
        
        // Calculate additional metrics
        int renewalLikelihood = calculateRenewalLikelihood(request, engagementScore);
        int upsellPotential = calculateUpsellPotential(request);

        return CustomerScoringResponse.builder()
                .customerId(request.getCustomerId())
                .customerName(request.getCustomerName())
                .valueLevel(valueLevel)
                .valueLevelLabel(CustomerScoringService.getValueLevelLabel(valueLevel))
                .engagementScore(engagementScore)
                .engagementLevel(CustomerScoringService.getEngagementLevel(engagementScore))
                .churnRisk(churnRisk)
                .churnRiskReason(getChurnRiskReason(churnRisk, request))
                .aiRecommendation(getRecommendation(valueLevel, engagementScore, churnRisk))
                .predictedRevenue(request.getTotalRevenue())
                .renewalLikelihood(renewalLikelihood)
                .upsellPotential(upsellPotential)
                .build();
    }

    private int calculateValueLevel(CustomerScoringRequest request) {
        double revenue = request.getTotalRevenue() != null ? request.getTotalRevenue().doubleValue() : 0;
        if (revenue >= 1000000) return 5;
        if (revenue >= 500000) return 4;
        if (revenue >= 100000) return 3;
        if (revenue >= 50000) return 2;
        return 1;
    }

    private int calculateEngagementScore(CustomerScoringRequest request) {
        int score = 50;
        
        // Deduct for inactivity
        int daysSinceContact = request.getDaysSinceLastContact() != null ? request.getDaysSinceLastContact() : 0;
        score -= Math.min(daysSinceContact / 3, 30);
        
        // Add for email responsiveness
        int emailRate = request.getEmailResponseRate() != null ? request.getEmailResponseRate() : 50;
        score += (emailRate / 10) * 3;
        
        // Add for meetings
        int meetings = request.getMeetingCount() != null ? request.getMeetingCount() : 0;
        score += Math.min(meetings * 8, 24);
        
        // Add for active opportunities
        int opps = request.getActiveOpportunities() != null ? request.getActiveOpportunities() : 0;
        score += Math.min(opps * 5, 15);
        
        return Math.max(0, Math.min(100, score));
    }

    private String calculateChurnRisk(CustomerScoringRequest request, int engagementScore) {
        int daysSinceContact = request.getDaysSinceLastContact() != null ? request.getDaysSinceLastContact() : 0;
        
        if (daysSinceContact > 90 || engagementScore < 20) {
            return "HIGH";
        }
        if (daysSinceContact > 60 || engagementScore < 35) {
            return "HIGH";
        }
        if (daysSinceContact > 30 || engagementScore < 50) {
            return "MEDIUM";
        }
        return "LOW";
    }

    private int calculateRenewalLikelihood(CustomerScoringRequest request, int engagementScore) {
        int likelihood = 50;
        likelihood += (engagementScore - 50) / 5;
        
        if (request.getTotalOrders() != null && request.getTotalOrders() > 10) {
            likelihood += 10;
        }
        
        return Math.max(0, Math.min(100, likelihood));
    }

    private int calculateUpsellPotential(CustomerScoringRequest request) {
        int potential = 30;
        
        if (request.getActiveOpportunities() != null && request.getActiveOpportunities() > 3) {
            potential += 20;
        }
        
        if (request.getMeetingCount() != null && request.getMeetingCount() > 5) {
            potential += 15;
        }
        
        if (request.getTotalRevenue() != null && request.getTotalRevenue().doubleValue() > 500000) {
            potential += 15;
        }
        
        return Math.max(0, Math.min(100, potential));
    }

    private String getChurnRiskReason(String risk, CustomerScoringRequest request) {
        return switch (risk) {
            case "HIGH" -> "客户长期未联系且互动率极低，建议立即采取挽留措施";
            case "MEDIUM" -> "客户跟进积极性下降，需要加强联系频率";
            default -> "客户关系稳定，保持常规维护即可";
        };
    }

    private String getRecommendation(int valueLevel, int engagementScore, String churnRisk) {
        if ("HIGH".equals(churnRisk)) {
            return "紧急：建议立即联系客户，了解需求变化并提供优惠政策";
        }
        if (valueLevel >= 4) {
            return "重要客户：建议安排高层拜访，加强战略合作关系";
        }
        if (engagementScore < 50) {
            return "建议增加跟进频率，通过邮件和电话保持联系";
        }
        return "维持现有服务节奏，定期推送有价值的信息";
    }

    private CustomerScoringResponse createDefaultResponse(CustomerScoringRequest request) {
        return CustomerScoringResponse.builder()
                .customerId(request.getCustomerId())
                .customerName(request.getCustomerName())
                .valueLevel(3)
                .valueLevelLabel("LV3-价值客户")
                .engagementScore(50)
                .engagementLevel("中")
                .churnRisk("MEDIUM")
                .churnRiskReason("数据不足，无法准确评估")
                .aiRecommendation("建议完善客户数据以获得更准确的评分")
                .predictedRevenue(request.getTotalRevenue())
                .renewalLikelihood(50)
                .upsellPotential(30)
                .build();
    }
}