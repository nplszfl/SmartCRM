package com.smartcrm.lead.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * Lead entity - represents a sales lead/prospect
 */
@Data
@TableName("leads")
public class Lead {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String company;
    private String industry; // Industry for AI scoring
    private String companySize; // SMALL, MEDIUM, LARGE, ENTERPRISE
    private Double annualRevenue; // Annual revenue for AI scoring
    private String title;
    private String source; // WEB, REFERRAL, CAMPAIGN, COLD_CALL, TRADE_SHOW

    private String status; // NEW, CONTACTED, QUALIFIED, UNQUALIFIED, CONVERTED
    private String rating; // HOT, WARM, COLD

    private Long ownerId; // Sales rep assigned to this lead
    private Long convertedAccountId; // If converted to Account
    private Long convertedContactId; // If converted to Contact
    private LocalDateTime convertedAt; // Timestamp when lead was converted

    private LocalDateTime lastContactDate;
    private LocalDateTime nextFollowUpDate;

    private String notes;
    private String aiScore; // A, B, C, D, F from AI scoring
    private Double aiScoreValue; // Numeric score 0-100

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
