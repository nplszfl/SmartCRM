package com.smartcrm.followup.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Follow-up task entity for smart follow-up management
 */
@Data
@TableName("followup_tasks")
public class FollowupTask implements Serializable {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long customerId;
    private String customerName;
    private Long opportunityId;

    private String taskType; // CALL, EMAIL, MEETING, DEMO, PROPOSAL
    private String priority; // HIGH, MEDIUM, LOW
    private String status; // PENDING, COMPLETED, OVERDUE, CANCELLED

    private String title;
    private String description;
    private String suggestedAction;

    private LocalDateTime dueAt;
    private LocalDateTime completedAt;
    private String completedNote;

    // AI recommended best follow-up time
    private LocalDateTime aiRecommendedTime;
    private String aiRecommendationReason;

    // Overdue warning
    private Integer overdueHours;
    private Boolean warningSent = false;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted = 0;
}