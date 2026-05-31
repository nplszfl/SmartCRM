package com.smartcrm.quote.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO for Quote line item
 */
@Data
public class QuoteLineItemResponse {

    private Long id;
    private Long quoteId;
    private Integer lineNumber;
    private Long productId;
    private String productName;
    private String productCode;
    private String description;
    private BigDecimal quantity;
    private BigDecimal unitPrice;
    private String unit;
    private BigDecimal discountPercent;
    private BigDecimal lineTotal;
    private Integer sortOrder;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}