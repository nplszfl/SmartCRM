package com.smartcrm.followup.dto;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Follow-up task creation request DTO
 */
@Data
public class FollowupTaskRequest implements Serializable {
    private Long customerId;
    private String customerName;
    private Long opportunityId;
    private String taskType;
    private String priority;
    private String title;
    private String description;
    private LocalDateTime dueAt;
}