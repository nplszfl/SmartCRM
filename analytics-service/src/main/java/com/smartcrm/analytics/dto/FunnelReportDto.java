package com.smartcrm.analytics.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Sales funnel report DTO.
 * <p>
 * Represents a stage-by-stage conversion funnel from raw leads all the way
 * down to closed-won opportunities. Each {@link FunnelStage} captures the
 * number of records that entered the stage, the conversion rate from the
 * previous stage, and the total monetary value of opportunities at the stage.
 */
@Data
public class FunnelReportDto {

    /** Period the funnel covers. */
    private LocalDateTime periodStart;
    private LocalDateTime periodEnd;

    /** Total leads that entered the top of the funnel. */
    private long totalLeads;

    /** Total opportunities created in the period. */
    private long totalOpportunities;

    /** Total deals closed-won in the period. */
    private long wonDeals;

    /** Overall lead-to-win conversion rate, expressed as a percentage (0-100). */
    private BigDecimal overallConversionRate;

    /** Total won revenue. */
    private BigDecimal totalWonRevenue;

    /** Per-stage funnel breakdown, ordered from top to bottom. */
    private List<FunnelStage> stages;

    @Data
    public static class FunnelStage {
        /** Stage name, e.g. "LEAD", "QUALIFIED_LEAD", "OPPORTUNITY", "PROPOSAL", "NEGOTIATION", "CLOSED_WON". */
        private String stage;

        /** Number of records at this stage. */
        private long count;

        /** Total monetary value of opportunities at this stage (zero for pure-lead stages). */
        private BigDecimal totalAmount;

        /**
         * Conversion rate from the *previous* stage, expressed as a percentage (0-100).
         * Null for the first stage since there is nothing to convert from.
         */
        private BigDecimal conversionFromPrevious;

        /**
         * Conversion rate from the *first* stage, expressed as a percentage (0-100).
         * Useful for the classic funnel chart.
         */
        private BigDecimal conversionFromTop;
    }
}
