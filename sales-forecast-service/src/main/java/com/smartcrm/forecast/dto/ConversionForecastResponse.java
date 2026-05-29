package com.smartcrm.forecast.dto;

import lombok.Data;
import lombok.Builder;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Opportunity conversion forecast DTO
 */
@Data
@Builder
public class ConversionForecastResponse implements Serializable {
    private Long opportunityId;
    private String opportunityName;

    private String currentStage;
    private BigDecimal amount;

    // Conversion predictions
    private Double conversionProbability;
    private String predictedOutcome; // WON, LOST, STALLED

    // Time predictions
    private LocalDateTime predictedCloseDate;
    private Integer remainingDays;

    // Recommendations
    private String recommendation;
    private Double riskScore;

    private LocalDateTime generatedAt;
}