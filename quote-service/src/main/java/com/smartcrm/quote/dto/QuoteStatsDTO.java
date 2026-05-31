package com.smartcrm.quote.dto;

import lombok.Data;

/**
 * DTO for quote statistics
 */
@Data
public class QuoteStatsDTO {

    private Long totalQuotes;
    private Long draftCount;
    private Long sentCount;
    private Long approvedCount;
    private Long acceptedCount;
    private Long expiredCount;
    private Long cancelledCount;

    private java.math.BigDecimal totalValue;
    private Double averageApprovalTime;
    private Double acceptanceRate;
    private java.math.BigDecimal averageAmount;
}