package com.smartcrm.contract.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO for Contract - separates API response from entity
 */
@Data
public class ContractResponse {
    private Long id;
    private String contractNumber;
    private String title;
    private String description;
    private Long accountId;
    private Long opportunityId;
    private Long contactId;
    private String status;
    private String type;
    private BigDecimal totalAmount;
    private BigDecimal paidAmount;
    private BigDecimal outstandingAmount;
    private BigDecimal taxAmount;
    private String paymentTerms;
    private LocalDateTime paymentDueDate;
    private LocalDateTime effectiveDate;
    private LocalDateTime expirationDate;
    private LocalDateTime signedDate;
    private Long ownerId;
    private String signatureData;
    private String aiRiskScore;
    private Double aiRiskConfidence;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}