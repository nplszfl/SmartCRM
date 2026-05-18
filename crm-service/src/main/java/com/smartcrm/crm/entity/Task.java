package com.smartcrm.crm.entity;

import com.smartcrm.common.entity.UserEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;

/**
 * Task entity - manages CRM tasks and activities
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tasks")
public class Task extends UserEntity {

    private String subject;
    private String description;
    private String status;            // NOT_STARTED, IN_PROGRESS, COMPLETED, DEFERRED, CANCELLED
    private String priority;          // LOW, MEDIUM, HIGH, URGENT
    private String category;          // CALL, EMAIL, MEETING, DEMO, PROPOSAL, FOLLOW_UP, OTHER
    
    // Related entities
    private Long accountId;
    private Long contactId;
    private Long opportunityId;
    private Long leadId;
    private Long campaignId;
    
    // Assignment
    private Long assignedToId;
    private String assignedToName;
    private Long createdById;
    private String createdByName;
    
    // Dates
    private LocalDateTime dueDate;
    private LocalDateTime startDate;
    private LocalDateTime completedDate;
    private LocalDateTime reminderDate;
    
    // Time tracking
    private Integer estimatedHours;
    private Integer actualHours;
    
    // Recurrence
    private String isRecurring;
    private String recurrencePattern;  // DAILY, WEEKLY, MONTHLY, YEARLY
    private Integer recurrenceInterval;
    
    // Location for meetings
    private String location;
    
    // Outcome
    private String outcome;
    private String notes;
    
    // Completion
    private String completionStatus;
    private Integer completionPercentage;
}
