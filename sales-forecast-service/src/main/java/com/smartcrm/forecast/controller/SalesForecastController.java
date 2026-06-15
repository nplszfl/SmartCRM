package com.smartcrm.forecast.controller;

import com.smartcrm.common.dto.ApiResponse;
import com.smartcrm.forecast.dto.ConversionForecastResponse;
import com.smartcrm.forecast.dto.ForecastAccuracyResponse;
import com.smartcrm.forecast.dto.MonthlyForecastResponse;
import com.smartcrm.forecast.dto.RecordActualRequest;
import com.smartcrm.forecast.dto.TargetCompletionResponse;
import com.smartcrm.forecast.entity.ForecastAccuracy;
import com.smartcrm.forecast.service.ForecastAccuracyService;
import com.smartcrm.forecast.service.SalesForecastService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * Sales forecast REST controller
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/forecast")
public class SalesForecastController {

    private final SalesForecastService salesForecastService;
    private final ForecastAccuracyService forecastAccuracyService;

    @GetMapping("/monthly")
    public ApiResponse<MonthlyForecastResponse> getMonthlyForecast(
            @RequestParam int year,
            @RequestParam int month) {
        return ApiResponse.success(salesForecastService.getMonthlyForecast(year, month));
    }

    @GetMapping("/quarterly")
    public ApiResponse<List<MonthlyForecastResponse>> getQuarterlyForecast(
            @RequestParam int year,
            @RequestParam int month,
            @RequestParam(defaultValue = "3") int months) {
        return ApiResponse.success(salesForecastService.getQuarterlyForecast(year, month, months));
    }

    @GetMapping("/conversion")
    public ApiResponse<ConversionForecastResponse> getConversionForecast(
            @RequestParam Long opportunityId,
            @RequestParam String opportunityName,
            @RequestParam String currentStage,
            @RequestParam BigDecimal amount) {
        return ApiResponse.success(salesForecastService.getConversionForecast(
                opportunityId, opportunityName, currentStage, amount));
    }

    @GetMapping("/target-completion")
    public ApiResponse<TargetCompletionResponse> getTargetCompletion(
            @RequestParam int year,
            @RequestParam int month) {
        return ApiResponse.success(salesForecastService.getTargetCompletion(year, month));
    }

    // -------- Forecast accuracy tracking & deviation alerts --------

    /**
     * Record an actual value for a given forecast + period. The service auto-computes
     * deviation, accuracy and an alert level.
     */
    @PostMapping("/accuracy")
    public ApiResponse<ForecastAccuracyResponse> recordActual(@Valid @RequestBody RecordActualRequest request) {
        ForecastAccuracy record = forecastAccuracyService.calculateAndRecord(request);
        return ApiResponse.success(forecastAccuracyService.getAccuracyByForecastId(record.getForecastId())
                .stream()
                .filter(r -> record.getPeriod().equals(r.getPeriod()))
                .findFirst()
                .orElseThrow());
    }

    /**
     * Get all accuracy records for a given forecastId.
     */
    @GetMapping("/accuracy/{forecastId}")
    public ApiResponse<List<ForecastAccuracyResponse>> getAccuracyByForecast(@PathVariable Long forecastId) {
        return ApiResponse.success(forecastAccuracyService.getAccuracyByForecastId(forecastId));
    }

    /**
     * Return forecasts whose accuracy falls below the given threshold (default 0.7).
     * Useful for surfacing forecasts that need a model retrain or human review.
     */
    @GetMapping("/accuracy/low")
    public ApiResponse<List<ForecastAccuracyResponse>> getLowAccuracyForecasts(
            @RequestParam(defaultValue = "0.7") double threshold) {
        return ApiResponse.success(forecastAccuracyService.getLowAccuracyForecasts(threshold));
    }
}