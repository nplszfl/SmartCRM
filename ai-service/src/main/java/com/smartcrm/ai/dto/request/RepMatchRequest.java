package com.smartcrm.ai.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RepMatchRequest {

    @NotBlank(message = "Lead ID is required")
    private String leadId;

    @NotBlank(message = "Company name is required")
    private String companyName;

    @NotBlank(message = "Industry is required")
    private String industry;

    @NotBlank(message = "Deal size is required")
    private String dealSize;

    private String geographicPreference;

    private String productInterest;

    private List<String> requiredSkills;
}