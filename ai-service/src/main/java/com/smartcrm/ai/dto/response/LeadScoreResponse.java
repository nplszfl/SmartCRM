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
public class LeadScoreResponse {
    private String leadId;
    private Integer score;
    private String grade;
    private String reasoning;
    private List<String> keyFactors;
    private List<String> recommendations;
}