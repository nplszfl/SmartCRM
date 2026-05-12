package com.smartcrm.analytics.controller;

import com.smartcrm.analytics.dto.PerformanceReportDto;
import com.smartcrm.analytics.dto.SalesDashboardDto;
import com.smartcrm.analytics.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
}
