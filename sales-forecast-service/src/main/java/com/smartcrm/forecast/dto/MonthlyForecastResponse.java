package com.smartcrm.forecast.dto;

import lombok.Data;
import lombok.Builder;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Monthly sales forecast response DTO
 */
@Data
@Builder
public class MonthlyForecastResponse implements Serializable {
    private Integer year;
    private Integer month;

    private BigDecimal predictedSales;
    private BigDecimal predictedWon;
    private BigDecimal predictedLost;
    private BigDecimal predictedNet;

    private Double confidenceLevel;
    private String confidenceLevelLabel;

    private String aiAnalysis;
    private String growthTrend;
    private Double growthRate;

    private LocalDateTime generatedAt;

    // Comparison with last period
    private BigDecimal vsLastMonth;
    private Double vsLastMonthPercent;
}