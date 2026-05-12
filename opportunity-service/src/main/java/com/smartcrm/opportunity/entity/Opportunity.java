package com.smartcrm.opportunity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Opportunity entity - represents a sales opportunity
 */
@Data
@TableName("opportunities")
public class Opportunity {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;
    private String description;

    private Long accountId;
    private Long contactId;
    private Long leadId;

    private String stage; // PROSPECTING, QUALIFICATION, PROPOSAL, NEGOTIATION, CLOSED_WON, CLOSED_LOST
    private BigDecimal amount;
    private BigDecimal probability;

    private Long ownerId;

    private LocalDateTime expectedCloseDate;
    private LocalDateTime actualCloseDate;

    private String type; // NEW_BUSINESS, EXISTING_BUSINESS
    private String source;

    private String aiPrediction; // HIGH, MEDIUM, LOW
    private Double aiConfidenceScore;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
