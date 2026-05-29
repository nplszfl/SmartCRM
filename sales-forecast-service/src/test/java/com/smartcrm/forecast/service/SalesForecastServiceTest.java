package com.smartcrm.forecast.service;

import com.smartcrm.forecast.dto.ConversionForecastResponse;
import com.smartcrm.forecast.dto.MonthlyForecastResponse;
import com.smartcrm.forecast.dto.TargetCompletionResponse;
import com.smartcrm.forecast.entity.SalesForecast;
import com.smartcrm.forecast.entity.SalesTarget;
import com.smartcrm.forecast.mapper.SalesForecastMapper;
import com.smartcrm.forecast.mapper.SalesTargetMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SalesForecastServiceTest {

    @Mock
    private SalesForecastMapper salesForecastMapper;

    @Mock
    private SalesTargetMapper salesTargetMapper;

    @Mock
    private AiForecastService aiForecastService;

    @InjectMocks
    private SalesForecastService salesForecastService;

    @Test
    void testGetMonthlyForecast_fromCache() {
        // Given
        SalesForecast cachedForecast = new SalesForecast();
        cachedForecast.setYear(2025);
        cachedForecast.setMonth(6);
        cachedForecast.setPredictedSales(new BigDecimal("1000000"));
        cachedForecast.setPredictedWon(new BigDecimal("600000"));
        cachedForecast.setPredictedLost(new BigDecimal("100000"));
        cachedForecast.setConfidenceLevel(0.8);
        cachedForecast.setAiAnalysis("Cached analysis");
        cachedForecast.setGrowthTrend("UP");
        cachedForecast.setGrowthRate(15.5);
        cachedForecast.setCreatedAt(LocalDateTime.now());

        when(salesForecastMapper.selectOne(any())).thenReturn(cachedForecast);

        // When
        MonthlyForecastResponse result = salesForecastService.getMonthlyForecast(2025, 6);

        // Then
        assertNotNull(result);
        assertEquals(2025, result.getYear());
        assertEquals(6, result.getMonth());
        assertEquals(new BigDecimal("1000000"), result.getPredictedSales());
        assertEquals("UP", result.getGrowthTrend());
        verify(aiForecastService, never()).predictMonthlySales(anyInt(), anyInt());
    }

    @Test
    void testGetMonthlyForecast_generateNew() {
        // Given
        when(salesForecastMapper.selectOne(any())).thenReturn(null);
        when(salesForecastMapper.insert(any())).thenReturn(1);

        MonthlyForecastResponse aiResponse = MonthlyForecastResponse.builder()
                .year(2025)
                .month(7)
                .predictedSales(new BigDecimal("1200000"))
                .predictedWon(new BigDecimal("720000"))
                .predictedLost(new BigDecimal("120000"))
                .confidenceLevel(0.75)
                .aiAnalysis("AI analysis")
                .growthTrend("UP")
                .growthRate(20.0)
                .generatedAt(LocalDateTime.now())
                .build();

        when(aiForecastService.predictMonthlySales(2025, 7)).thenReturn(aiResponse);

        // When
        MonthlyForecastResponse result = salesForecastService.getMonthlyForecast(2025, 7);

        // Then
        assertNotNull(result);
        assertEquals(2025, result.getYear());
        assertEquals(7, result.getMonth());
        verify(salesForecastMapper, times(1)).insert(any());
    }

    @Test
    void testGetConversionForecast() {
        // Given
        ConversionForecastResponse aiResponse = ConversionForecastResponse.builder()
                .opportunityId(1L)
                .opportunityName("Test Opportunity")
                .currentStage("PROPOSAL")
                .amount(new BigDecimal("500000"))
                .conversionProbability(0.7)
                .predictedOutcome("WON")
                .remainingDays(30)
                .recommendation("Proceed to negotiation")
                .riskScore(0.3)
                .build();

        when(aiForecastService.predictConversion(any(), any(), any(), any())).thenReturn(aiResponse);

        // When
        ConversionForecastResponse result = salesForecastService.getConversionForecast(
                1L, "Test Opportunity", "PROPOSAL", new BigDecimal("500000"));

        // Then
        assertNotNull(result);
        assertEquals("WON", result.getPredictedOutcome());
        assertEquals(0.7, result.getConversionProbability());
    }

    @Test
    void testGetTargetCompletion_withTargets() {
        // Given
        SalesTarget target1 = new SalesTarget();
        target1.setYear(2025);
        target1.setMonth(6);
        target1.setOwnerId(1L);
        target1.setTargetAmount(new BigDecimal("500000"));
        target1.setAchievedAmount(new BigDecimal("300000"));
        target1.setPipelineValue(new BigDecimal("150000"));
        target1.setStatus("ON_TRACK");

        SalesTarget target2 = new SalesTarget();
        target2.setYear(2025);
        target2.setMonth(6);
        target2.setOwnerId(2L);
        target2.setTargetAmount(new BigDecimal("500000"));
        target2.setAchievedAmount(new BigDecimal("200000"));
        target2.setPipelineValue(new BigDecimal("100000"));
        target2.setStatus("AT_RISK");

        when(salesTargetMapper.selectList(any())).thenReturn(List.of(target1, target2));

        // When
        TargetCompletionResponse result = salesForecastService.getTargetCompletion(2025, 6);

        // Then
        assertNotNull(result);
        assertEquals(new BigDecimal("1000000"), result.getTargetAmount());
        assertEquals(new BigDecimal("500000"), result.getAchievedAmount());
        assertEquals(2, result.getTotalOwners());
        assertEquals(1, result.getOnTrackCount());
        assertEquals(1, result.getAtRiskCount());
    }

    @Test
    void testGetTargetCompletion_noTargets() {
        // Given
        when(salesTargetMapper.selectList(any())).thenReturn(List.of());

        // When
        TargetCompletionResponse result = salesForecastService.getTargetCompletion(2025, 6);

        // Then
        assertNotNull(result);
        assertEquals("NO_TARGET", result.getStatus());
    }

    @Test
    void testGetQuarterlyForecast() {
        // Given
        when(salesForecastMapper.selectOne(any())).thenReturn(null);
        when(salesForecastMapper.insert(any())).thenReturn(1);

        when(aiForecastService.predictMonthlySales(anyInt(), anyInt()))
                .thenReturn(MonthlyForecastResponse.builder()
                        .year(2025)
                        .month(7)
                        .predictedSales(new BigDecimal("1000000"))
                        .predictedWon(new BigDecimal("600000"))
                        .predictedLost(new BigDecimal("100000"))
                        .confidenceLevel(0.7)
                        .generatedAt(LocalDateTime.now())
                        .build());

        // When
        List<MonthlyForecastResponse> results = salesForecastService.getQuarterlyForecast(2025, 7, 3);

        // Then
        assertNotNull(results);
        assertEquals(3, results.size());
    }
}