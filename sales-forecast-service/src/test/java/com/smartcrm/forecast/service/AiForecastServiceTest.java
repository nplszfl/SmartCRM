package com.smartcrm.forecast.service;

import com.smartcrm.forecast.dto.ConversionForecastResponse;
import com.smartcrm.forecast.dto.MonthlyForecastResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class AiForecastServiceTest {

    private AiForecastService aiForecastService;

    @BeforeEach
    void setUp() {
        aiForecastService = new AiForecastService();
    }

    @Test
    void testPredictMonthlySales_returnsValidPrediction() {
        // When
        MonthlyForecastResponse result = aiForecastService.predictMonthlySales(2025, 6);

        // Then
        assertNotNull(result);
        assertEquals(2025, result.getYear());
        assertEquals(6, result.getMonth());
        assertNotNull(result.getPredictedSales());
        assertTrue(result.getPredictedSales().compareTo(BigDecimal.ZERO) > 0);
        assertNotNull(result.getPredictedWon());
        assertNotNull(result.getPredictedLost());
        assertNotNull(result.getConfidenceLevel());
    }

    @Test
    void testPredictMonthlySales_decemberHasHigherPrediction() {
        // When
        MonthlyForecastResponse result = aiForecastService.predictMonthlySales(2025, 12);

        // Then
        assertNotNull(result);
        assertEquals("UP", result.getGrowthTrend());
        assertTrue(result.getGrowthRate() > 0);
    }

    @Test
    void testPredictConversion_proposalStage() {
        // When
        ConversionForecastResponse result = aiForecastService.predictConversion(
                1L, "Test Opportunity", "PROPOSAL", new BigDecimal("500000"));

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getOpportunityId());
        assertEquals("PROPOSAL", result.getCurrentStage());
        assertTrue(result.getConversionProbability() >= 0.5);
        assertNotNull(result.getPredictedOutcome());
        assertNotNull(result.getRemainingDays());
    }

    @Test
    void testPredictConversion_earlyStage() {
        // When - PROSPECTING stage
        ConversionForecastResponse result = aiForecastService.predictConversion(
                1L, "Test", "PROSPECTING", new BigDecimal("100000"));

        // Then
        assertNotNull(result);
        assertTrue(result.getConversionProbability() < 0.5);
        assertTrue(result.getPredictedOutcome().equals("LOST") || result.getPredictedOutcome().equals("STALLED"));
    }

    @Test
    void testPredictConversion_negotiationStage() {
        // When - NEGOTIATION stage (high probability)
        ConversionForecastResponse result = aiForecastService.predictConversion(
                1L, "Hot Deal", "NEGOTIATION", new BigDecimal("800000"));

        // Then
        assertNotNull(result);
        assertTrue(result.getConversionProbability() >= 0.7);
        assertEquals("WON", result.getPredictedOutcome());
    }

    @Test
    void testPredictMonthlySales_januaryLowerPrediction() {
        // When
        MonthlyForecastResponse result = aiForecastService.predictMonthlySales(2025, 1);

        // Then
        assertNotNull(result);
        assertEquals("DOWN", result.getGrowthTrend());
    }
}