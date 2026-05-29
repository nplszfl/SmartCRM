package com.smartcrm.contract.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Contract entity - represents a sales contract with a customer
 */
@Data
@TableName("contracts")
public class Contract {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String contractNumber;
    private String title;
    private String description;

    private Long accountId;
    private Long opportunityId;
    private Long contactId;

    private String status; // DRAFT, PENDING, ACTIVE, EXPIRED, TERMINATED, CANCELLED
    private String type; // SALES, SERVICE, NDA, PARTNERSHIP, OTHER
    
    private BigDecimal totalAmount;
    private BigDecimal paidAmount;
    private BigDecimal taxAmount;
    
    private String paymentTerms;
    private LocalDateTime paymentDueDate;

    private LocalDateTime effectiveDate;
    private LocalDateTime expirationDate;
    private LocalDateTime signedDate;

    private Long ownerId;
    private String signatureData;
    
    private String aiRiskScore; // HIGH, MEDIUM, LOW
    private Double aiRiskConfidence;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
