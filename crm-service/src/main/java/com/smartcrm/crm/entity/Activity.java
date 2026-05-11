package com.smartcrm.crm.entity;

import com.smartcrm.common.entity.UserEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Activity entity.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("activities")
public class Activity extends UserEntity {

    private Long customerId;
    private Long contactId;
    private String activityType;  // CALL, EMAIL, MEETING, NOTE, TASK, EVENT
    private String subject;
    private String description;
    private LocalDateTime activityDate;
    private Integer durationMinutes;
    private String location;
    private String outcome;
    private String nextAction;
    private LocalDate nextActionDate;
    private Long ownerId;
}