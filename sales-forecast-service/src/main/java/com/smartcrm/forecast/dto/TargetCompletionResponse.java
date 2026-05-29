package com.smartcrm.forecast.dto;

import lombok.Data;
import lombok.Builder;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Sales target completion analysis DTO
 */
@Data
@Builder
public class TargetCompletionResponse implements Serializable {
    private Integer year;
    private Integer month;

    private BigDecimal targetAmount;
    private BigDecimal achievedAmount;
    private BigDecimal remainingAmount;

    private Double completionRate;
    private String status; // ON_TRACK, AT_RISK, BEHIND, ACHIEVED

    private String statusReason;
    private Double probabilityToAchieve;

    // Projections
    private BigDecimal projectedAchievement;
    private BigDecimal shortfall;

    // Breakdown by owner
    private Integer totalOwners;
    private Integer onTrackCount;
    private Integer atRiskCount;
    private Integer behindCount;
}