package com.smartcrm.forecast.controller;

import com.smartcrm.common.dto.ApiResponse;
import com.smartcrm.forecast.dto.ConversionForecastResponse;
import com.smartcrm.forecast.dto.MonthlyForecastResponse;
import com.smartcrm.forecast.dto.TargetCompletionResponse;
import com.smartcrm.forecast.service.SalesForecastService;
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
}