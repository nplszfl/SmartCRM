package com.smartcrm.forecast.service;

import com.smartcrm.forecast.dto.ConversionForecastResponse;
import com.smartcrm.forecast.dto.MonthlyForecastResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * AI-powered sales forecasting service (mock implementation for demo)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiForecastService {

    /**
     * Predict monthly sales using AI (rule-based fallback)
     */
    public MonthlyForecastResponse predictMonthlySales(int year, int month) {
        log.info("Predicting monthly sales for {}/{}", year, month);

        // Use historical pattern with seasonal adjustment
        BigDecimal baseAmount = BigDecimal.valueOf(1000000);
        double seasonalFactor = getSeasonalFactor(month);

        BigDecimal predictedSales = baseAmount.multiply(BigDecimal.valueOf(seasonalFactor))
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal predictedWon = predictedSales.multiply(BigDecimal.valueOf(0.6))
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal predictedLost = predictedSales.multiply(BigDecimal.valueOf(0.1))
                .setScale(2, RoundingMode.HALF_UP);

        String growthTrend = getGrowthTrend(month);
        double growthRate = calculateGrowthRate(month);

        return MonthlyForecastResponse.builder()
                .year(year)
                .month(month)
                .predictedSales(predictedSales)
                .predictedWon(predictedWon)
                .predictedLost(predictedLost)
                .predictedNet(predictedWon.subtract(predictedLost))
                .confidenceLevel(0.65)
                .confidenceLevelLabel("中")
                .aiAnalysis(buildMonthlyAnalysis(predictedSales, month))
                .growthTrend(growthTrend)
                .growthRate(growthRate)
                .generatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Predict opportunity conversion probability
     */
    public ConversionForecastResponse predictConversion(Long opportunityId, String opportunityName,
            String currentStage, BigDecimal amount) {
        log.info("Predicting conversion for opportunity: {} in stage {}", opportunityId, currentStage);

        double probability = getStageProbability(currentStage);
        String outcome = predictOutcome(probability);
        int remainingDays = getStageRemainingDays(currentStage);
        double riskScore = calculateRiskScore(currentStage, amount);

        LocalDate predictedDate = LocalDate.now().plusDays(remainingDays);

        return ConversionForecastResponse.builder()
                .opportunityId(opportunityId)
                .opportunityName(opportunityName)
                .currentStage(currentStage)
                .amount(amount)
                .conversionProbability(probability)
                .predictedOutcome(outcome)
                .predictedCloseDate(predictedDate.atStartOfDay())
                .remainingDays(remainingDays)
                .recommendation(getStageRecommendation(currentStage))
                .riskScore(riskScore)
                .generatedAt(LocalDateTime.now())
                .build();
    }

    private double getSeasonalFactor(int month) {
        Map<Integer, Double> seasonalMap = new HashMap<>();
        seasonalMap.put(1, 0.8);
        seasonalMap.put(2, 0.8);
        seasonalMap.put(3, 0.9);
        seasonalMap.put(4, 1.0);
        seasonalMap.put(5, 1.0);
        seasonalMap.put(6, 1.0);
        seasonalMap.put(7, 0.9);
        seasonalMap.put(8, 0.9);
        seasonalMap.put(9, 1.1);
        seasonalMap.put(10, 1.2);
        seasonalMap.put(11, 1.3);
        seasonalMap.put(12, 1.4);
        return seasonalMap.getOrDefault(month, 1.0);
    }

    private String getGrowthTrend(int month) {
        if (month >= 10) return "UP";
        if (month >= 7) return "FLAT";
        return "DOWN";
    }

    private double calculateGrowthRate(int month) {
        return switch (month) {
            case 10 -> 15.0;
            case 11 -> 18.0;
            case 12 -> 20.0;
            case 4, 5 -> 5.0;
            default -> 0.0;
        };
    }

    private String buildMonthlyAnalysis(BigDecimal predictedSales, int month) {
        StringBuilder analysis = new StringBuilder();
        analysis.append("基于历史销售数据和市场趋势分析，");
        analysis.append(String.format("预测本月销售额将达到 %.2f 万元。", predictedSales.divide(BigDecimal.valueOf(10000), 2, RoundingMode.HALF_UP)));

        if (month >= 10) {
            analysis.append("第四季度通常为销售旺季，预计成交率将有所提升。");
        } else if (month <= 2) {
            analysis.append("年初淡季，建议加大营销投入并做好客户储备。");
        } else {
            analysis.append("保持稳定跟进节奏，关注Pipeline中的重点商机。");
        }

        return analysis.toString();
    }

    private double getStageProbability(String stage) {
        Map<String, Double> stageMap = new HashMap<>();
        stageMap.put("PROSPECTING", 0.2);
        stageMap.put("QUALIFICATION", 0.4);
        stageMap.put("PROPOSAL", 0.6);
        stageMap.put("NEGOTIATION", 0.8);
        stageMap.put("CLOSED_WON", 1.0);
        stageMap.put("CLOSED_LOST", 0.0);
        return stageMap.getOrDefault(stage, 0.5);
    }

    private String predictOutcome(double probability) {
        if (probability >= 0.7) return "WON";
        if (probability <= 0.2) return "LOST";
        return "STALLED";
    }

    private int getStageRemainingDays(String stage) {
        Map<String, Integer> stageDays = new HashMap<>();
        stageDays.put("PROSPECTING", 60);
        stageDays.put("QUALIFICATION", 45);
        stageDays.put("PROPOSAL", 30);
        stageDays.put("NEGOTIATION", 14);
        return stageDays.getOrDefault(stage, 30);
    }

    private double calculateRiskScore(String stage, BigDecimal amount) {
        double baseScore = 1.0 - getStageProbability(stage);

        // Larger deals have more risk
        if (amount != null && amount.compareTo(BigDecimal.valueOf(500000)) > 0) {
            baseScore += 0.1;
        }

        return Math.min(1.0, baseScore);
    }

    private String getStageRecommendation(String stage) {
        Map<String, String> recommendations = new HashMap<>();
        recommendations.put("PROSPECTING", "需要更多资格确认，建议进行需求诊断电话，明确客户痛点和预算");
        recommendations.put("QUALIFICATION", "明确采购时间线和决策链，准备针对性解决方案");
        recommendations.put("PROPOSAL", "突出方案价值和ROI，准备竞争性报价策略");
        recommendations.put("NEGOTIATION", "关注客户核心顾虑，准备适当的让步方案促进成交");
        return recommendations.getOrDefault(stage, "保持当前跟进节奏，等待时机推进");
    }
}