package com.smartcrm.forecast.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Request DTO for recording an actual value against a forecast.
 * The service uses (forecastId, period) as a natural key to compute the accuracy.
 */
@Data
public class RecordActualRequest {
    @NotNull(message = "forecastId is required")
    private Long forecastId;

    @NotBlank(message = "period is required")
    private String period;

    @NotNull(message = "actual is required")
    private BigDecimal actual;
}
