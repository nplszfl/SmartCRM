package com.smartcrm.crm.entity;

import com.smartcrm.common.entity.UserEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Campaign entity - manages marketing campaigns
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("campaigns")
public class Campaign extends UserEntity {

    private String name;
    private String type;              // EMAIL, SOCIAL, EVENT, WEBINAR, AD, PARTNER
    private String status;            // PLANNING, ACTIVE, PAUSED, COMPLETED, CANCELLED
    private String description;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private BigDecimal budget;
    private BigDecimal spent;
    private String currency;
    private Long ownerId;
    private String ownerName;
    
    // Target metrics
    private Integer targetLeads;
    private Integer targetConversions;
    private Integer targetRevenue;
    
    // Actual metrics
    private Integer impressions;
    private Integer clicks;
    private Integer leadsGenerated;
    private Integer conversions;
    private BigDecimal revenue;
    
    // Tracking
    private String source;
    private String medium;
    private String content;
    private String campaignCode;
    
    // Channels
    private String channel;           // EMAIL, SOCIAL_MEDIA, PAID_ADS, CONTENT, EVENTS
    private String targetAudience;
    private String objective;         // AWARENESS, CONSIDERATION, CONVERSION
    
    // Integration
    private String externalCampaignId;
    private String utmSource;
    private String utmMedium;
    private String utmCampaign;
    
    // AI insights
    private String aiRecommendation;
    private Double predictedRoi;
}
