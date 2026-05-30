package com.smartcrm.email.dto;

import lombok.Data;
import lombok.Builder;
import java.time.LocalDateTime;

/**
 * Response DTO for email operations
 */
@Data
@Builder
public class EmailResponse {
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
    private String status;
    private String type;
    private Long sentBy;
    private LocalDateTime sentAt;
    private LocalDateTime deliveredAt;
    private LocalDateTime openedAt;
    private LocalDateTime clickedAt;
    private LocalDateTime repliedAt;
    private String aiGenerated;
    private String aiPromptUsed;
    private String templateId;
    private String trackingId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}