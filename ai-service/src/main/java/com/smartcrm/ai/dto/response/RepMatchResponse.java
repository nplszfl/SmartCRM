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
public class RepMatchResponse {
    private String leadId;
    private String matchedRepId;
    private String matchedRepName;
    private Double matchScore;
    private List<AlternativeRep> alternativeReps;
    private String reasoning;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AlternativeRep {
        private String repId;
        private String repName;
        private Double score;
    }
}