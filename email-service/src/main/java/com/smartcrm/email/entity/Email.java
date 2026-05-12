package com.smartcrm.email.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * Email entity - represents an email in the system
 */
@Data
@TableName("emails")
public class Email {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long leadId;
    private Long contactId;
    private Long opportunityId;

    private String fromAddress;
    private String toAddress;
    private String ccAddress;
    private String bccAddress;

    private String subject;
    private String body;
    private String htmlBody;

    private String status; // DRAFT, SENT, DELIVERED, OPENED, CLICKED, REPLIED, BOUNCED, FAILED
    private String type; // OUTBOUND, INBOUND

    private Long sentBy; // User ID

    private LocalDateTime sentAt;
    private LocalDateTime deliveredAt;
    private LocalDateTime openedAt;
    private LocalDateTime clickedAt;
    private LocalDateTime repliedAt;

    private String aiGenerated; // YES, NO
    private String aiPromptUsed;

    private String templateId;
    private String trackingId;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
