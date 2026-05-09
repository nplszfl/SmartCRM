package com.smartcrm.ai.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DealPredictResponse {
    private String dealId;
    private Double closeProbability;
    private String riskLevel;
    private List<String> riskFactors;
    private List<String> recommendations;
    private String predictedCloseDate;
}