package com.smartcrm.opportunity.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * Request DTO for creating/updating an opportunity
 */
@Data
public class OpportunityRequest {
    private Long id;

    @NotBlank(message = "Opportunity name is required")
    private String name;

    private String description;

    private Long accountId;
    private Long contactId;
    private Long leadId;

    private String stage;
    
    @NotNull(message = "Amount is required")
    private BigDecimal amount;

    private BigDecimal probability;

    private Long ownerId;

    private String expectedCloseDate;
    private String type;
    private String source;
}
