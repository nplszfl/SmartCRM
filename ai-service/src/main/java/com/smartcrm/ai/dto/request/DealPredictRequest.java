package com.smartcrm.ai.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DealPredictRequest {

    @NotBlank(message = "Deal ID is required")
    private String dealId;

    @NotBlank(message = "Deal name is required")
    private String dealName;

    @NotBlank(message = "Account name is required")
    private String accountName;

    @NotBlank(message = "Stage is required")
    private String stage;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private Double amount;

    @NotBlank(message = "Owner ID is required")
    private String ownerId;

    private List<String> productLines;

    private String competitorInvolved;

    private List<HistoricalDeal> historicalWonDeals;

    private DealEngagementData engagementData;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HistoricalDeal {
        private String name;
        private Double amount;
        private Boolean won;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DealEngagementData {
        private Integer meetingsHeld;
        private Integer emailsExchanged;
        private Boolean demoCompleted;
        private Boolean proposalSent;
        private Integer daysSinceLastContact;
    }
}