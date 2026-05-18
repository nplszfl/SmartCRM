package com.smartcrm.crm.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Campaign request DTO.
 */
@Data
public class CampaignRequest {
    private String name;
    private String type;
    private String status;
    private String description;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private BigDecimal budget;
    private String currency;
    private Long ownerId;
    private String ownerName;
    private Integer targetLeads;
    private Integer targetConversions;
    private Integer targetRevenue;
    private String source;
    private String channel;
    private String targetAudience;
    private String objective;
    private String campaignCode;
}
