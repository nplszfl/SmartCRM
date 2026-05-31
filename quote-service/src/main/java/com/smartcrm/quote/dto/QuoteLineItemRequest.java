package com.smartcrm.quote.dto;

import lombok.Data;
import java.math.BigDecimal;

/**
 * Request DTO for quote line item
 */
@Data
public class QuoteLineItemRequest {

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
    private Integer sortOrder;
}