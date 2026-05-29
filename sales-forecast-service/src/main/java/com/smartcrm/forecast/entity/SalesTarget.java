package com.smartcrm.forecast.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Sales target entity for tracking goals
 */
@Data
@TableName("sales_targets")
public class SalesTarget implements Serializable {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Integer year;
    private Integer month;
    private Long ownerId;
    private String ownerName;

    private BigDecimal targetAmount;
    private BigDecimal achievedAmount;
    private BigDecimal pipelineValue;

    private Double completionRate;
    private String status; // ON_TRACK, AT_RISK, BEHIND

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted = 0;
}