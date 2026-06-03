package com.smartcrm.analytics.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Time-series trend data point for a single period (day / week / month).
 */
@Data
public class TrendPointDto {
    /** Period label start (inclusive). */
    private LocalDate periodStart;

    /** Numeric value for the period (revenue, count, etc). */
    private BigDecimal value;

    /** Optional count metric for the same period (e.g. number of deals closed). */
    private long count;
}
