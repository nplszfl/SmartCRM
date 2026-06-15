package com.smartcrm.forecast.service;

import com.smartcrm.common.exception.ResourceNotFoundException;
import com.smartcrm.forecast.dto.ForecastAccuracyResponse;
import com.smartcrm.forecast.dto.RecordActualRequest;
import com.smartcrm.forecast.entity.ForecastAccuracy;
import com.smartcrm.forecast.entity.SalesForecast;
import com.smartcrm.forecast.mapper.ForecastAccuracyMapper;
import com.smartcrm.forecast.mapper.SalesForecastMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link ForecastAccuracyService}.
 *
 * Covers:
 * - computeAccuracy: perfect match, small deviation, large deviation, zero predicted, null inputs
 * - deriveAlertLevel: OK / WARN / CRITICAL thresholds
 * - calculateAndRecord: insert when no existing row, update when row exists, throw when forecast missing,
 *   throw when forecast has no predicted value
 * - getLowAccuracyForecasts: filters by threshold, validates threshold range, orders by accuracy asc
 * - getAccuracyByForecastId: returns rows for given id, empty list when none
 */
@ExtendWith(MockitoExtension.class)
class ForecastAccuracyServiceTest {

    @Mock
    private ForecastAccuracyMapper forecastAccuracyMapper;

    @Mock
    private SalesForecastMapper salesForecastMapper;

    private ForecastAccuracyService service;

    @BeforeEach
    void setUp() {
        service = new ForecastAccuracyService(forecastAccuracyMapper, salesForecastMapper);
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private SalesForecast forecast(Long id, BigDecimal predictedSales) {
        SalesForecast f = new SalesForecast();
        f.setId(id);
        f.setPredictedSales(predictedSales);
        return f;
    }

    private ForecastAccuracy existing(Long forecastId, String period, double accuracy, String alertLevel) {
        ForecastAccuracy a = new ForecastAccuracy();
        a.setId(99L);
        a.setForecastId(forecastId);
        a.setPeriod(period);
        a.setAccuracy(accuracy);
        a.setAlertLevel(alertLevel);
        a.setCreatedAt(LocalDateTime.now().minusDays(1));
        a.setUpdatedAt(LocalDateTime.now().minusDays(1));
        return a;
    }

    // ==================== computeAccuracy (pure) ====================

    @Test
    @DisplayName("computeAccuracy: perfect match returns 1.0")
    void computeAccuracy_perfect() {
        BigDecimal p = new BigDecimal("1000.00");
        assertThat(ForecastAccuracyService.computeAccuracy(p, p)).isEqualTo(1.0);
    }

    @Test
    @DisplayName("computeAccuracy: zero predicted uses floor of 1 to avoid div-by-zero")
    void computeAccuracy_zeroPredicted() {
        BigDecimal zero = BigDecimal.ZERO;
        // diff=100, denominator=max(0,1)=1, accuracy = 1 - 100/1 = -99 -> clamped to 0
        assertThat(ForecastAccuracyService.computeAccuracy(zero, new BigDecimal("100"))).isEqualTo(0.0);
    }

    @Test
    @DisplayName("computeAccuracy: null inputs return 0.0")
    void computeAccuracy_null() {
        assertThat(ForecastAccuracyService.computeAccuracy(null, new BigDecimal("1"))).isEqualTo(0.0);
        assertThat(ForecastAccuracyService.computeAccuracy(new BigDecimal("1"), null)).isEqualTo(0.0);
    }

    @Test
    @DisplayName("computeAccuracy: 10% deviation yields 0.9")
    void computeAccuracy_tenPercent() {
        assertThat(ForecastAccuracyService.computeAccuracy(
                new BigDecimal("100"), new BigDecimal("110"))).isCloseTo(0.9, org.assertj.core.data.Offset.offset(0.001));
    }

    // ==================== deriveAlertLevel (pure) ====================

    @Test
    @DisplayName("deriveAlertLevel: maps accuracy ranges to OK / WARN / CRITICAL")
    void deriveAlertLevel_boundaries() {
        assertThat(ForecastAccuracyService.deriveAlertLevel(0.95)).isEqualTo("OK");
        assertThat(ForecastAccuracyService.deriveAlertLevel(0.90)).isEqualTo("OK");
        assertThat(ForecastAccuracyService.deriveAlertLevel(0.89)).isEqualTo("WARN");
        assertThat(ForecastAccuracyService.deriveAlertLevel(0.70)).isEqualTo("WARN");
        assertThat(ForecastAccuracyService.deriveAlertLevel(0.69)).isEqualTo("CRITICAL");
        assertThat(ForecastAccuracyService.deriveAlertLevel(0.0)).isEqualTo("CRITICAL");
    }

    // ==================== calculateAndRecord ====================

    @Test
    @DisplayName("calculateAndRecord: inserts a new accuracy row when none exists")
    void calculateAndRecord_inserts() {
        when(salesForecastMapper.selectById(1L)).thenReturn(forecast(1L, new BigDecimal("1000")));
        when(forecastAccuracyMapper.selectOne(any())).thenReturn(null);

        RecordActualRequest req = new RecordActualRequest();
        req.setForecastId(1L);
        req.setPeriod("2026-Q1");
        req.setActual(new BigDecimal("900"));

        ForecastAccuracy result = service.calculateAndRecord(req);

        ArgumentCaptor<ForecastAccuracy> captor = ArgumentCaptor.forClass(ForecastAccuracy.class);
        verify(forecastAccuracyMapper).insert(captor.capture());
        ForecastAccuracy saved = captor.getValue();
        assertThat(saved.getForecastId()).isEqualTo(1L);
        assertThat(saved.getPeriod()).isEqualTo("2026-Q1");
        assertThat(saved.getPredicted()).isEqualByComparingTo("1000");
        assertThat(saved.getActual()).isEqualByComparingTo("900");
        assertThat(saved.getDeviation()).isEqualByComparingTo("-100");
        // 10% deviation -> accuracy = 0.9 exactly -> "OK" (>= 0.9 is OK)
        assertThat(saved.getAccuracy()).isEqualTo(0.9);
        assertThat(saved.getAlertLevel()).isEqualTo("OK");
        assertThat(result).isSameAs(saved);
    }

    @Test
    @DisplayName("calculateAndRecord: updates existing accuracy row instead of inserting")
    void calculateAndRecord_updates() {
        when(salesForecastMapper.selectById(1L)).thenReturn(forecast(1L, new BigDecimal("1000")));
        when(forecastAccuracyMapper.selectOne(any()))
                .thenReturn(existing(1L, "2026-Q1", 0.5, "CRITICAL"));

        RecordActualRequest req = new RecordActualRequest();
        req.setForecastId(1L);
        req.setPeriod("2026-Q1");
        req.setActual(new BigDecimal("1000")); // perfect this time

        ForecastAccuracy result = service.calculateAndRecord(req);

        verify(forecastAccuracyMapper, never()).insert(any());
        verify(forecastAccuracyMapper).updateById(any(ForecastAccuracy.class));
        assertThat(result.getAccuracy()).isEqualTo(1.0);
        assertThat(result.getAlertLevel()).isEqualTo("OK");
    }

    @Test
    @DisplayName("calculateAndRecord: throws when forecast id does not exist")
    void calculateAndRecord_missingForecast() {
        when(salesForecastMapper.selectById(42L)).thenReturn(null);

        RecordActualRequest req = new RecordActualRequest();
        req.setForecastId(42L);
        req.setPeriod("2026-Q1");
        req.setActual(new BigDecimal("100"));

        assertThatThrownBy(() -> service.calculateAndRecord(req))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("calculateAndRecord: throws when forecast has no predicted value")
    void calculateAndRecord_noPredicted() {
        when(salesForecastMapper.selectById(1L)).thenReturn(forecast(1L, null));

        RecordActualRequest req = new RecordActualRequest();
        req.setForecastId(1L);
        req.setPeriod("2026-Q1");
        req.setActual(new BigDecimal("100"));

        assertThatThrownBy(() -> service.calculateAndRecord(req))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no predicted value");
    }

    // ==================== getLowAccuracyForecasts ====================

    @Test
    @DisplayName("getLowAccuracyForecasts: validates threshold range")
    void getLowAccuracyForecasts_invalidThreshold() {
        assertThatThrownBy(() -> service.getLowAccuracyForecasts(-0.1))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.getLowAccuracyForecasts(1.5))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("getLowAccuracyForecasts: returns DTO list ordered by accuracy asc")
    void getLowAccuracyForecasts_returnsList() {
        ForecastAccuracy a1 = existing(1L, "2026-Q1", 0.3, "CRITICAL");
        ForecastAccuracy a2 = existing(2L, "2026-Q1", 0.6, "WARN");
        when(forecastAccuracyMapper.selectList(any()))
                .thenReturn(Arrays.asList(a1, a2));

        List<ForecastAccuracyResponse> result = service.getLowAccuracyForecasts(0.7);

        assertThat(result).hasSize(2);
        // Worst first (mapper order is preserved).
        assertThat(result.get(0).getAccuracy()).isEqualTo(0.3);
        assertThat(result.get(0).getAccuracyLabel()).isEqualTo("严重偏差");
        assertThat(result.get(1).getAccuracy()).isEqualTo(0.6);
        assertThat(result.get(1).getAccuracyLabel()).isEqualTo("偏差");
    }

    @Test
    @DisplayName("getLowAccuracyForecasts: returns empty list when no rows below threshold")
    void getLowAccuracyForecasts_empty() {
        when(forecastAccuracyMapper.selectList(any())).thenReturn(Collections.emptyList());

        assertThat(service.getLowAccuracyForecasts(0.5)).isEmpty();
    }

    // ==================== getAccuracyByForecastId ====================

    @Test
    @DisplayName("getAccuracyByForecastId: returns DTOs for the given forecast id")
    void getAccuracyByForecastId_returnsList() {
        when(forecastAccuracyMapper.selectList(any()))
                .thenReturn(Arrays.asList(
                        existing(5L, "2026-Q1", 0.95, "OK"),
                        existing(5L, "2026-Q2", 0.80, "WARN")));

        List<ForecastAccuracyResponse> result = service.getAccuracyByForecastId(5L);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getForecastId()).isEqualTo(5L);
        // 0.95 -> 极佳, 0.80 -> 一般
        assertThat(result.get(0).getAccuracyLabel()).isEqualTo("极佳");
        assertThat(result.get(1).getAccuracyLabel()).isEqualTo("一般");
    }
}
