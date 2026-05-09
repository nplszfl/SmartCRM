package com.smartcrm.common.dto;

import lombok.Data;
import lombok.Builder;
import java.io.Serializable;

/**
 * AI insight result DTO for analytics.
 */
@Data
@Builder
public class AIInsightResult implements Serializable {

    private String insightType;     // TREND, ANOMALY, RECOMMENDATION
    private String title;
    private String description;
    private Double confidence;
    private String[] recommendations;
    private Object data;            // Supporting data
}