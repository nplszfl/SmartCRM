package com.smartcrm.forecast.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Forecast accuracy entity - tracks predicted vs actual sales and the resulting accuracy score.
 * One row per (forecastId, period) measurement.
 */
@Data
@TableName("forecast_accuracy")
public class ForecastAccuracy implements Serializable {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** Logical foreign key to sales_forecasts.id. */
    private Long forecastId;

    /** Period identifier, e.g. "2025-06". */
    private String period;

    /** Predicted value at the time the forecast was made. */
    private BigDecimal predicted;

    /** Actual realized value. */
    private BigDecimal actual;

    /** Absolute deviation: actual - predicted (signed, can be negative). */
    private BigDecimal deviation;

    /** Accuracy score in [0, 1]. 1.0 = perfect prediction, 0.0 = worst case. */
    private Double accuracy;

    /** Alert level derived from accuracy: OK, WARN, CRITICAL. */
    private String alertLevel;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted = 0;
}
