package com.smartcrm.quote.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.smartcrm.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * Quote line item entity - represents a line item in a quote
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_quote_line_item")
public class QuoteLineItem extends BaseEntity {

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
}