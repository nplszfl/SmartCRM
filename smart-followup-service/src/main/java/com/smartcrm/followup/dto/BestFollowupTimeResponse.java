package com.smartcrm.followup.dto;

import lombok.Data;
import lombok.Builder;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Best follow-up time suggestion DTO
 */
@Data
@Builder
public class BestFollowupTimeResponse implements Serializable {
    private Long customerId;
    private String customerName;

    private LocalDateTime suggestedTime;
    private String dayOfWeek;
    private String timeSlot;

    private String reason;
    private Double confidence;
    private String alternativeTimes;
}