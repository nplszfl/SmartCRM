package com.smartcrm.ai.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailGenerateRequest {

    @NotBlank(message = "Lead ID is required")
    private String leadId;

    @NotBlank(message = "Contact name is required")
    private String contactName;

    @Email(message = "Valid email is required")
    private String contactEmail;

    @NotBlank(message = "Company name is required")
    private String companyName;

    @Builder.Default
    private String templateType = "initial_outreach";

    private Boolean abTesting = true;

    private EmailPersonalizationData personalizationData;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EmailPersonalizationData {
        private String senderName;
        private String industry;
        private String valueProposition;
        private String specificBenefit;
        private String topic;
        private String socialProof;
        private String benefit;
        private String companySimilar;
        private String solution;
        private String goalsSummary;
        private String timeframe;
        private String result;
        private String agendaItem1;
        private String agendaItem2;
        private String agendaItem3;
        private String timeOption1;
        private String timeOption2;
        private String timeOption3;
        private String proposalHighlight1;
        private String proposalHighlight2;
        private String proposalHighlight3;
        private String personalizationTopic;
        private String goal;
    }
}