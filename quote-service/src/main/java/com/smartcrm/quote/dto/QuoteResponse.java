package com.smartcrm.quote.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for Quote
 */
@Data
public class QuoteResponse {

    private Long id;
    private String quoteNumber;
    private Long opportunityId;
    private Long accountId;
    private Long contactId;
    private String title;
    private String status;
    private Integer version;
    private LocalDateTime validFrom;
    private LocalDateTime validUntil;

    private BigDecimal subtotal;
    private BigDecimal taxRate;
    private BigDecimal taxAmount;
    private BigDecimal discountPercent;
    private BigDecimal discountAmount;
    private BigDecimal totalAmount;

    private String currency;
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

    private Boolean aiGenerated;
    private Double aiConfidenceScore;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private List<QuoteLineItemResponse> lineItems;
}