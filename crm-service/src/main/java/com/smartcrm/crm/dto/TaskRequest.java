package com.smartcrm.crm.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * Task request DTO.
 */
@Data
public class TaskRequest {
    private String subject;
    private String description;
    private String status;
    private String priority;
    private String category;
    
    private Long accountId;
    private Long contactId;
    private Long opportunityId;
    private Long leadId;
    private Long campaignId;
    
    private Long assignedToId;
    private String assignedToName;
    
    private LocalDateTime dueDate;
    private LocalDateTime startDate;
    private LocalDateTime reminderDate;
    
    private Integer estimatedHours;
    private String isRecurring;
    private String recurrencePattern;
    private Integer recurrenceInterval;
    
    private String location;
    private String notes;
}
