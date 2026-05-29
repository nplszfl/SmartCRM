package com.smartcrm.customer.dto;

import lombok.Data;
import lombok.Builder;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Customer scoring response DTO
 */
@Data
@Builder
public class CustomerScoringResponse implements Serializable {
    private Long customerId;
    private String customerName;

    // Value Level: LV1-LV5
    private Integer valueLevel;
    private String valueLevelLabel;

    // Engagement Score: 0-100
    private Integer engagementScore;
    private String engagementLevel;

    // Churn Risk: HIGH, MEDIUM, LOW
    private String churnRisk;
    private String churnRiskReason;

    // AI recommendations
    private String aiRecommendation;

    // Detailed metrics
    private BigDecimal predictedRevenue;
    private Integer renewalLikelihood;
    private Integer upsellPotential;
}