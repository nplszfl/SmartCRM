package com.smartcrm.forecast.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for forecast accuracy metrics returned to the API consumer.
 */
@Data
@Builder
public class ForecastAccuracyResponse {
    private Long forecastId;
    private String period;
    private BigDecimal predicted;
    private BigDecimal actual;
    private BigDecimal deviation;
    private Double accuracy;
    private String alertLevel;
    private String accuracyLabel;
    private LocalDateTime measuredAt;
}
