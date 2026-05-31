package com.smartcrm.quote.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Request DTO for creating/updating a quote
 */
@Data
public class QuoteRequest {

    private Long opportunityId;
    private Long accountId;
    private Long contactId;
    private String title;
    private String status;
    private LocalDateTime validFrom;
    private LocalDateTime validUntil;

    private BigDecimal subtotal;
    private BigDecimal taxRate;
    private BigDecimal discountPercent;
    private String currency;
    private String paymentTerms;
    private String deliveryTerms;
    private String notes;
    private String termsAndConditions;
    private String internalNotes;
    private Long ownerId;
    private String ownerName;

    private List<LineItemRequest> lineItems;

    @Data
    public static class LineItemRequest {
        private Long productId;
        private String productName;
        private String productCode;
        private String description;
        private BigDecimal quantity;
        private BigDecimal unitPrice;
        private String unit;
        private BigDecimal discountPercent;
    }
}