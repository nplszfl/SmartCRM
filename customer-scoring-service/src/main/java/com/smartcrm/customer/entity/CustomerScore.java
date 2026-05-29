package com.smartcrm.customer.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Customer scoring entity with AI-powered evaluation
 */
@Data
@TableName("customer_scores")
public class CustomerScore implements Serializable {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long customerId;
    private String customerName;

    // Value Level: LV1-LV5
    private Integer valueLevel; // 1-5

    // Engagement Score: 0-100
    private Integer engagementScore;

    // Churn Risk: HIGH, MEDIUM, LOW
    private String churnRisk;

    // Detailed scores
    private BigDecimal totalRevenue;
    private Integer totalOrders;
    private Integer activeOpportunities;

    // AI analysis summary
    private String aiAnalysis;
    private Double aiConfidence;

    private LocalDateTime lastScoreAt;
    private LocalDateTime nextScoreAt;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted = 0;
}