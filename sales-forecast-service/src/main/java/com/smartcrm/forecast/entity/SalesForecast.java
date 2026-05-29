package com.smartcrm.forecast.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Sales forecast entity
 */
@Data
@TableName("sales_forecasts")
public class SalesForecast implements Serializable {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Integer year;
    private Integer month;

    // Monthly sales prediction
    private BigDecimal predictedSales;
    private BigDecimal predictedWon;
    private BigDecimal predictedLost;

    // Confidence level
    private Double confidenceLevel;
    private String confidenceReason;

    // AI analysis
    private String aiAnalysis;
    private String growthTrend;
    private Double growthRate;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted = 0;
}