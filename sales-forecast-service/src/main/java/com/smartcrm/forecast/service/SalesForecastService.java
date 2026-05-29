package com.smartcrm.forecast.service;

import com.smartcrm.forecast.dto.ConversionForecastResponse;
import com.smartcrm.forecast.dto.MonthlyForecastResponse;
import com.smartcrm.forecast.dto.TargetCompletionResponse;
import com.smartcrm.forecast.entity.SalesForecast;
import com.smartcrm.forecast.entity.SalesTarget;
import com.smartcrm.forecast.mapper.SalesForecastMapper;
import com.smartcrm.forecast.mapper.SalesTargetMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Sales forecast service with AI-powered predictions
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SalesForecastService {

    private final SalesForecastMapper salesForecastMapper;
    private final SalesTargetMapper salesTargetMapper;
    private final AiForecastService aiForecastService;

    /**
     * Get monthly sales forecast
     */
    public MonthlyForecastResponse getMonthlyForecast(int year, int month) {
        log.info("Getting monthly forecast for {}/{}", year, month);

        // Check cache first
        SalesForecast existing = salesForecastMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SalesForecast>()
                        .eq(SalesForecast::getYear, year)
                        .eq(SalesForecast::getMonth, month)
        );

        if (existing != null) {
            return mapToMonthlyResponse(existing);
        }

        // Generate new forecast using AI
        MonthlyForecastResponse response = aiForecastService.predictMonthlySales(year, month);

        // Save forecast
        saveForecast(response);

        return response;
    }

    /**
     * Get conversion forecast for an opportunity
     */
    public ConversionForecastResponse getConversionForecast(Long opportunityId, String opportunityName,
            String currentStage, BigDecimal amount) {
        log.info("Getting conversion forecast for opportunity: {}", opportunityId);
        return aiForecastService.predictConversion(opportunityId, opportunityName, currentStage, amount);
    }

    /**
     * Get sales target completion analysis
     */
    public TargetCompletionResponse getTargetCompletion(int year, int month) {
        log.info("Getting target completion for {}/{}", year, month);

        List<SalesTarget> targets = salesTargetMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SalesTarget>()
                        .eq(SalesTarget::getYear, year)
                        .eq(SalesTarget::getMonth, month)
        );

        return calculateCompletion(targets, year, month);
    }

    /**
     * Get forecast for next N months
     */
    public List<MonthlyForecastResponse> getQuarterlyForecast(int startYear, int startMonth, int months) {
        log.info("Getting {} months forecast starting from {}/{}", months, startYear, startMonth);

        java.util.List<MonthlyForecastResponse> forecasts = new java.util.ArrayList<>();
        int year = startYear;
        int month = startMonth;

        for (int i = 0; i < months; i++) {
            forecasts.add(getMonthlyForecast(year, month));
            month++;
            if (month > 12) {
                month = 1;
                year++;
            }
        }

        return forecasts;
    }

    private void saveForecast(MonthlyForecastResponse response) {
        SalesForecast forecast = new SalesForecast();
        forecast.setYear(response.getYear());
        forecast.setMonth(response.getMonth());
        forecast.setPredictedSales(response.getPredictedSales());
        forecast.setPredictedWon(response.getPredictedWon());
        forecast.setPredictedLost(response.getPredictedLost());
        forecast.setConfidenceLevel(response.getConfidenceLevel());
        forecast.setAiAnalysis(response.getAiAnalysis());
        forecast.setGrowthTrend(response.getGrowthTrend());
        forecast.setGrowthRate(response.getGrowthRate());

        salesForecastMapper.insert(forecast);
    }

    private MonthlyForecastResponse mapToMonthlyResponse(SalesForecast forecast) {
        return MonthlyForecastResponse.builder()
                .year(forecast.getYear())
                .month(forecast.getMonth())
                .predictedSales(forecast.getPredictedSales())
                .predictedWon(forecast.getPredictedWon())
                .predictedLost(forecast.getPredictedLost())
                .predictedNet(forecast.getPredictedWon().subtract(forecast.getPredictedLost()))
                .confidenceLevel(forecast.getConfidenceLevel())
                .confidenceLevelLabel(getConfidenceLabel(forecast.getConfidenceLevel()))
                .aiAnalysis(forecast.getAiAnalysis())
                .growthTrend(forecast.getGrowthTrend())
                .growthRate(forecast.getGrowthRate())
                .generatedAt(forecast.getCreatedAt())
                .build();
    }

    private TargetCompletionResponse calculateCompletion(List<SalesTarget> targets, int year, int month) {
        if (targets.isEmpty()) {
            return TargetCompletionResponse.builder()
                    .year(year)
                    .month(month)
                    .targetAmount(BigDecimal.ZERO)
                    .achievedAmount(BigDecimal.ZERO)
                    .remainingAmount(BigDecimal.ZERO)
                    .completionRate(0.0)
                    .status("NO_TARGET")
                    .statusReason("本月暂无销售目标")
                    .probabilityToAchieve(0.0)
                    .projectedAchievement(BigDecimal.ZERO)
                    .shortfall(BigDecimal.ZERO)
                    .totalOwners(0)
                    .onTrackCount(0)
                    .atRiskCount(0)
                    .behindCount(0)
                    .build();
        }

        BigDecimal totalTarget = targets.stream().map(SalesTarget::getTargetAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalAchieved = targets.stream().map(SalesTarget::getAchievedAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal remaining = totalTarget.subtract(totalAchieved);

        double completionRate = totalTarget.compareTo(BigDecimal.ZERO) > 0
                ? totalAchieved.divide(totalTarget, 4, RoundingMode.HALF_UP).doubleValue() * 100
                : 0;

        long onTrack = targets.stream().filter(t -> "ON_TRACK".equals(t.getStatus())).count();
        long atRisk = targets.stream().filter(t -> "AT_RISK".equals(t.getStatus())).count();
        long behind = targets.stream().filter(t -> "BEHIND".equals(t.getStatus())).count();

        String status;
        if (completionRate >= 100) {
            status = "ACHIEVED";
        } else if (completionRate >= 80) {
            status = "ON_TRACK";
        } else if (completionRate >= 50) {
            status = "AT_RISK";
        } else {
            status = "BEHIND";
        }

        BigDecimal projectedAchievement = totalAchieved.add(
                targets.stream().map(SalesTarget::getPipelineValue).reduce(BigDecimal.ZERO, BigDecimal::add)
        );

        return TargetCompletionResponse.builder()
                .year(year)
                .month(month)
                .targetAmount(totalTarget)
                .achievedAmount(totalAchieved)
                .remainingAmount(remaining)
                .completionRate(completionRate)
                .status(status)
                .statusReason(getStatusReason(status))
                .probabilityToAchieve(calculateProbability(completionRate, targets.size()))
                .projectedAchievement(projectedAchievement)
                .shortfall(totalTarget.subtract(projectedAchievement).max(BigDecimal.ZERO))
                .totalOwners(targets.size())
                .onTrackCount((int) onTrack)
                .atRiskCount((int) atRisk)
                .behindCount((int) behind)
                .build();
    }

    private String getConfidenceLabel(Double confidence) {
        if (confidence == null) return "未知";
        if (confidence >= 0.9) return "非常高";
        if (confidence >= 0.7) return "高";
        if (confidence >= 0.5) return "中";
        if (confidence >= 0.3) return "低";
        return "非常低";
    }

    private String getStatusReason(String status) {
        return switch (status) {
            case "ACHIEVED" -> "目标已达成，继续保持";
            case "ON_TRACK" -> "进度正常，保持当前节奏";
            case "AT_RISK" -> "存在风险，需要加强跟进";
            case "BEHIND" -> "落后较多，需要立即行动";
            default -> "暂无足够数据";
        };
    }

    private double calculateProbability(double completionRate, int ownerCount) {
        double baseProbability = completionRate / 100.0;
        double ownerFactor = Math.min(1.0, ownerCount / 10.0);
        return Math.min(1.0, baseProbability + (1 - baseProbability) * 0.3 * ownerFactor);
    }
}