package com.smartcrm.common.dto;

import lombok.Data;
import lombok.Builder;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * AI prediction result DTO.
 */
@Data
@Builder
public class AIPredictionResult implements Serializable {

    private Double probability;     // 0-1
    private String prediction;      // e.g., "CLOSE_WON", "CLOSED_LOST"
    private String reasoning;
    private String[] riskFactors;
    private String recommendedNextAction;
    private LocalDateTime predictedCloseDate;
    private Double expectedValue;
}