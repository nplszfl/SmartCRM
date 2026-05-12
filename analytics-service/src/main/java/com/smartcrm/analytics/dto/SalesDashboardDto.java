package com.smartcrm.analytics.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Sales dashboard data DTO
 */
@Data
public class SalesDashboardDto {
    private long totalLeads;
    private long hotLeads;
    private long convertedLeads;
    private long totalOpportunities;
    private long openOpportunities;
    private long wonOpportunities;
    private long lostOpportunities;

    private BigDecimal totalPipelineValue;
    private BigDecimal weightedPipelineValue;
    private BigDecimal wonRevenue;
    private BigDecimal lostRevenue;

    private BigDecimal averageDealSize;
    private BigDecimal winRate;

    private List<StageDistribution> stageDistribution;
    private List<LeadSourceAnalysis> leadSourceAnalysis;
    private Map<String, Long> leadsByStatus;
    private Map<String, Long> opportunitiesByStage;
}

/**
 * Opportunity stage distribution
 */
@Data
class StageDistribution {
    private String stage;
    private long count;
    private BigDecimal totalAmount;
    private BigDecimal percentage;
}

/**
 * Lead source analysis
 */
@Data
class LeadSourceAnalysis {
    private String source;
    private long leadCount;
    private long conversionCount;
    private BigDecimal conversionRate;
}
