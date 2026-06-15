package com.smartcrm.forecast.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartcrm.common.exception.ResourceNotFoundException;
import com.smartcrm.forecast.dto.ForecastAccuracyResponse;
import com.smartcrm.forecast.dto.RecordActualRequest;
import com.smartcrm.forecast.entity.ForecastAccuracy;
import com.smartcrm.forecast.entity.SalesForecast;
import com.smartcrm.forecast.mapper.ForecastAccuracyMapper;
import com.smartcrm.forecast.mapper.SalesForecastMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Tracks prediction accuracy for sales forecasts and emits deviation alerts
 * when realized values diverge significantly from predicted values.
 *
 * <p>Accuracy formula (relative):
 * <pre>accuracy = max(0, 1 - |actual - predicted| / max(|predicted|, 1))</pre>
 *
 * <p>Alert levels (default threshold 0.7):
 * <ul>
 *   <li>OK:        accuracy >= 0.9</li>
 *   <li>WARN:      0.7 &lt;= accuracy &lt; 0.9</li>
 *   <li>CRITICAL:  accuracy &lt; 0.7</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ForecastAccuracyService {

    /** Default low-accuracy threshold used by {@link #getLowAccuracyForecasts}. */
    public static final double DEFAULT_LOW_ACCURACY_THRESHOLD = 0.7;

    private final ForecastAccuracyMapper forecastAccuracyMapper;
    private final SalesForecastMapper salesForecastMapper;

    /**
     * Record an actual value for a given forecast + period and compute the accuracy score.
     * If an accuracy row already exists for the same (forecastId, period) it is updated.
     *
     * @param request record-actual request payload
     * @return computed accuracy record
     * @throws ResourceNotFoundException if the referenced {@code forecastId} doesn't exist
     */
    @Transactional
    public ForecastAccuracy calculateAndRecord(RecordActualRequest request) {
        log.info("Recording actual for forecastId={} period={} actual={}",
                request.getForecastId(), request.getPeriod(), request.getActual());

        // 1. Look up the original forecast to get the predicted value.
        SalesForecast forecast = salesForecastMapper.selectById(request.getForecastId());
        if (forecast == null) {
            throw new ResourceNotFoundException("SalesForecast", request.getForecastId());
        }
        BigDecimal predicted = forecast.getPredictedSales();
        if (predicted == null) {
            // Fall back to predictedWon if predictedSales is missing.
            predicted = forecast.getPredictedWon();
        }
        if (predicted == null) {
            throw new IllegalStateException(
                    "Forecast " + request.getForecastId() + " has no predicted value (predictedSales/predictedWon).");
        }

        BigDecimal actual = request.getActual();
        BigDecimal deviation = actual.subtract(predicted);
        double accuracy = computeAccuracy(predicted, actual);
        String alertLevel = deriveAlertLevel(accuracy);

        // 2. Find existing accuracy row for (forecastId, period) - upsert semantics.
        ForecastAccuracy existing = forecastAccuracyMapper.selectOne(
                new LambdaQueryWrapper<ForecastAccuracy>()
                        .eq(ForecastAccuracy::getForecastId, request.getForecastId())
                        .eq(ForecastAccuracy::getPeriod, request.getPeriod())
        );

        LocalDateTime now = LocalDateTime.now();
        if (existing == null) {
            ForecastAccuracy record = new ForecastAccuracy();
            record.setForecastId(request.getForecastId());
            record.setPeriod(request.getPeriod());
            record.setPredicted(predicted);
            record.setActual(actual);
            record.setDeviation(deviation);
            record.setAccuracy(accuracy);
            record.setAlertLevel(alertLevel);
            record.setCreatedAt(now);
            record.setUpdatedAt(now);
            forecastAccuracyMapper.insert(record);
            log.info("Inserted forecast accuracy id={} accuracy={} alert={}",
                    record.getId(), accuracy, alertLevel);
            return record;
        } else {
            existing.setPredicted(predicted);
            existing.setActual(actual);
            existing.setDeviation(deviation);
            existing.setAccuracy(accuracy);
            existing.setAlertLevel(alertLevel);
            existing.setUpdatedAt(now);
            forecastAccuracyMapper.updateById(existing);
            log.info("Updated forecast accuracy id={} accuracy={} alert={}",
                    existing.getId(), accuracy, alertLevel);
            return existing;
        }
    }

    /**
     * Return all accuracy records whose accuracy is strictly below the threshold,
     * ordered from worst to best accuracy.
     */
    public List<ForecastAccuracyResponse> getLowAccuracyForecasts(double threshold) {
        if (threshold < 0.0 || threshold > 1.0) {
            throw new IllegalArgumentException("threshold must be in [0, 1], got: " + threshold);
        }
        log.info("Fetching low-accuracy forecasts below threshold={}", threshold);
        List<ForecastAccuracy> records = forecastAccuracyMapper.selectList(
                new LambdaQueryWrapper<ForecastAccuracy>()
                        .lt(ForecastAccuracy::getAccuracy, threshold)
                        .orderByAsc(ForecastAccuracy::getAccuracy)
        );
        return records.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Return all accuracy records for a given forecastId, ordered by period ascending.
     */
    public List<ForecastAccuracyResponse> getAccuracyByForecastId(Long forecastId) {
        log.info("Fetching accuracy for forecastId={}", forecastId);
        List<ForecastAccuracy> records = forecastAccuracyMapper.selectList(
                new LambdaQueryWrapper<ForecastAccuracy>()
                        .eq(ForecastAccuracy::getForecastId, forecastId)
                        .orderByAsc(ForecastAccuracy::getPeriod)
        );
        return records.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // --- Pure math helpers (package-private for testability) ---

    /**
     * Compute the accuracy score in [0, 1] for a given (predicted, actual) pair.
     * Uses relative deviation with a floor of 1 on the denominator to avoid division-by-zero
     * when the predicted value is 0.
     */
    static double computeAccuracy(BigDecimal predicted, BigDecimal actual) {
        if (predicted == null || actual == null) {
            return 0.0;
        }
        BigDecimal denominator = predicted.abs().max(BigDecimal.ONE);
        BigDecimal diff = actual.subtract(predicted).abs();
        BigDecimal ratio = diff.divide(denominator, 8, RoundingMode.HALF_UP);
        // accuracy = 1 - relative_deviation, clamped to [0, 1]
        double raw = BigDecimal.ONE.subtract(ratio).doubleValue();
        if (Double.isNaN(raw) || raw < 0.0) return 0.0;
        if (raw > 1.0) return 1.0;
        return raw;
    }

    static String deriveAlertLevel(double accuracy) {
        if (accuracy < 0.7) return "CRITICAL";
        if (accuracy < 0.9) return "WARN";
        return "OK";
    }

    static String getAccuracyLabel(double accuracy) {
        if (accuracy >= 0.95) return "极佳";
        if (accuracy >= 0.85) return "良好";
        if (accuracy >= 0.7)  return "一般";
        if (accuracy >= 0.5)  return "偏差";
        return "严重偏差";
    }

    private ForecastAccuracyResponse toResponse(ForecastAccuracy a) {
        return ForecastAccuracyResponse.builder()
                .forecastId(a.getForecastId())
                .period(a.getPeriod())
                .predicted(a.getPredicted())
                .actual(a.getActual())
                .deviation(a.getDeviation())
                .accuracy(a.getAccuracy())
                .alertLevel(a.getAlertLevel())
                .accuracyLabel(getAccuracyLabel(a.getAccuracy() == null ? 0.0 : a.getAccuracy()))
                .measuredAt(a.getCreatedAt())
                .build();
    }
}
