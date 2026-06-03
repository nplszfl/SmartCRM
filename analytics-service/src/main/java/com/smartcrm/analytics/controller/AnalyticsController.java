package com.smartcrm.analytics.controller;

import com.smartcrm.analytics.dto.FunnelReportDto;
import com.smartcrm.analytics.dto.PerformanceReportDto;
import com.smartcrm.analytics.dto.RankingEntryDto;
import com.smartcrm.analytics.dto.SalesDashboardDto;
import com.smartcrm.analytics.dto.TrendPointDto;
import com.smartcrm.analytics.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Analytics REST controller
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/dashboard")
    public SalesDashboardDto getSalesDashboard() {
        return analyticsService.getSalesDashboard();
    }

    @GetMapping("/performance/{userId}")
    public PerformanceReportDto getUserPerformance(
            @PathVariable Long userId,
            @RequestParam(required = false, defaultValue = "USER") String userName,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        if (startDate == null) {
            startDate = LocalDateTime.now().minusMonths(1);
        }
        if (endDate == null) {
            endDate = LocalDateTime.now();
        }

        return analyticsService.getUserPerformance(userId, userName, startDate, endDate);
    }

    @GetMapping("/forecast")
    public BigDecimal getPipelineForecast(@RequestParam(defaultValue = "3") int monthsAhead) {
        return analyticsService.getPipelineForecast(monthsAhead);
    }

    @GetMapping("/conversion-rates")
    public Map<String, BigDecimal> getConversionRatesBySource() {
        return analyticsService.getConversionRatesBySource();
    }

    @GetMapping("/time-to-conversion")
    public Map<String, Object> getAverageTimeToConversion() {
        return Map.of(
                "averageDays", analyticsService.getAverageTimeToConversion(),
                "unit", "days"
        );
    }

    @GetMapping("/sales-cycle")
    public Map<String, Object> getAverageSalesCycleDuration() {
        return Map.of(
                "averageDays", analyticsService.getAverageSalesCycleDuration(),
                "unit", "days"
        );
    }

    @GetMapping("/activity/{userId}")
    public Map<String, Long> getActivitySummary(
            @PathVariable Long userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        if (startDate == null) {
            startDate = LocalDateTime.now().minusMonths(1);
        }
        if (endDate == null) {
            endDate = LocalDateTime.now();
        }

        return analyticsService.getActivitySummary(userId, startDate, endDate);
    }

    /**
     * Get the lead-to-won sales funnel for a given period.
     * <p>
     * Stage order: LEAD → QUALIFIED_LEAD → OPPORTUNITY → PROPOSAL →
     * NEGOTIATION → CLOSED_WON. Each stage includes count, monetary value,
     * and conversion rates (from previous stage, and from the top of the funnel).
     */
    @GetMapping("/funnel")
    public FunnelReportDto getFunnel(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        if (startDate == null) startDate = LocalDateTime.now().minusMonths(3);
        if (endDate == null) endDate = LocalDateTime.now();
        return analyticsService.getFunnelReport(startDate, endDate);
    }

    /**
     * Get a dense revenue trend series.
     *
     * @param granularity DAY (default), WEEK, or MONTH
     */
    @GetMapping("/trend/revenue")
    public List<TrendPointDto> getRevenueTrend(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(required = false, defaultValue = "DAY") String granularity) {
        if (startDate == null) startDate = LocalDateTime.now().minusMonths(1);
        if (endDate == null) endDate = LocalDateTime.now();
        return analyticsService.getRevenueTrend(startDate, endDate, granularity);
    }

    /**
     * Get the top sales-rep leaderboard for a given period, ranked by won revenue.
     */
    @GetMapping("/ranking/top-performers")
    public List<RankingEntryDto> getTopPerformers(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(required = false, defaultValue = "10") int limit) {
        if (startDate == null) startDate = LocalDateTime.now().minusMonths(3);
        if (endDate == null) endDate = LocalDateTime.now();
        return analyticsService.getTopPerformers(startDate, endDate, limit);
    }
}
