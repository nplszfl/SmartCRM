package com.smartcrm.ai.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeadScoreRequest {

    @NotBlank(message = "Lead ID is required")
    private String leadId;

    @NotBlank(message = "Company name is required")
    private String companyName;

    @NotBlank(message = "Contact name is required")
    private String contactName;

    private String contactTitle;

    private String industry;

    private String companySize;

    private String revenue;

    private String source;

    private LeadBehavioralData behavioralData;

    private LeadFirmographicData firmographicData;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LeadBehavioralData {
        private Integer emailsOpened;
        private Integer pagesViewed;
        private Integer eventsAttended;
        private Integer webinarsAttended;
        private Integer downloads;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LeadFirmographicData {
        private Integer employeeCount;
        private Integer annualRevenue;
        private String marketSegment;
        private String techStack;
    }
}