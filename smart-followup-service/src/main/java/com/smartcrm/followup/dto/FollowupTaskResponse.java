package com.smartcrm.followup.dto;

import lombok.Data;
import lombok.Builder;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Follow-up task response DTO
 */
@Data
@Builder
public class FollowupTaskResponse implements Serializable {
    private Long id;
    private Long customerId;
    private String customerName;
    private Long opportunityId;

    private String taskType;
    private String priority;
    private String status;

    private String title;
    private String description;
    private String suggestedAction;

    private LocalDateTime dueAt;
    private LocalDateTime completedAt;

    // AI recommended follow-up time
    private LocalDateTime aiRecommendedTime;
    private String aiRecommendationReason;

    // Overdue info
    private Integer overdueHours;
    private String overdueWarning;
}