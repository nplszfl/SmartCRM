package com.smartcrm.analytics.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Performance report DTO
 */
@Data
public class PerformanceReportDto {
    private Long userId;
    private String userName;

    private long leadsOwned;
    private long opportunitiesOwned;
    private long dealsWon;
    private long dealsLost;

    private BigDecimal totalSalesValue;
    private BigDecimal averageDealSize;
    private BigDecimal winRate;

    private long emailsSent;
    private long emailsOpened;
    long emailsReplied;
    private BigDecimal emailOpenRate;
    private BigDecimal emailReplyRate;

    private LocalDateTime periodStart;
    private LocalDateTime periodEnd;
}
