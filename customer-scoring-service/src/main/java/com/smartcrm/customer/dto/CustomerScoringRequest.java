package com.smartcrm.customer.dto;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Customer scoring request DTO
 */
@Data
public class CustomerScoringRequest implements Serializable {
    private Long customerId;
    private String customerName;
    private BigDecimal totalRevenue;
    private Integer totalOrders;
    private Integer activeOpportunities;
    private Integer daysSinceLastContact;
    private Integer emailResponseRate;
    private Integer meetingCount;
}