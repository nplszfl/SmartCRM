package com.smartcrm.analytics.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * Single row in a "top performers" / leaderboard report.
 * <p>
 * Used by the ranking analytics endpoint to display the best sales reps by
 * a chosen metric (revenue, deals won, conversion rate, ...).
 */
@Data
public class RankingEntryDto {
    /** 1-based rank within the result set. */
    private int rank;

    /** Sales rep id. */
    private Long userId;

    /** Sales rep display name. */
    private String userName;

    /** Primary metric value (revenue, deal count, etc) — the metric the report is sorted by. */
    private BigDecimal metricValue;

    /** Optional secondary metric (e.g. win rate alongside revenue). */
    private BigDecimal secondaryMetric;

    /** Optional context metric (e.g. total leads owned). */
    private long contextCount;
}
