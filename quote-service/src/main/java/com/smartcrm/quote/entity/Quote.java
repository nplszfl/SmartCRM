package com.smartcrm.quote.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.smartcrm.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Quote entity - represents a quote/proposal for a customer
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_quote")
public class Quote extends BaseEntity {

    private String quoteNumber;

    private Long opportunityId;
    private Long accountId;
    private Long contactId;

    private String title;
    private String status;

    private Integer version = 1;

    private LocalDateTime validFrom;
    private LocalDateTime validUntil;

    private BigDecimal subtotal;
    private BigDecimal taxRate = BigDecimal.ZERO;
    private BigDecimal taxAmount;
    private BigDecimal discountPercent = BigDecimal.ZERO;
    private BigDecimal discountAmount;
    private BigDecimal totalAmount;

    private String currency = "USD";

    private String paymentTerms;
    private String deliveryTerms;

    private String notes;
    private String termsAndConditions;
    private String internalNotes;

    private Long ownerId;
    private String ownerName;

    private String approvedBy;
    private LocalDateTime approvedAt;
    private LocalDateTime sentAt;
    private LocalDateTime viewedAt;
    private LocalDateTime lastFollowupAt;

    private Boolean aiGenerated = false;
    private Double aiConfidenceScore;
}