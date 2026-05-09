package com.smartcrm.common.dto;

import lombok.Data;
import lombok.Builder;
import java.io.Serializable;

/**
 * AI scoring result DTO.
 */
@Data
@Builder
public class AIScoreResult implements Serializable {

    private Double score;           // 0-100
    private String reasoning;       // AI-generated explanation
    private String confidence;      // HIGH, MEDIUM, LOW
    private String[] factors;       // Key scoring factors
    private String recommendedAction;
}