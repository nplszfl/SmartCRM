package com.smartcrm.contract.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * Request DTO for creating/updating a contract
 */
@Data
public class ContractRequest {
    private Long id;

    @NotBlank(message = "Contract number is required")
    private String contractNumber;

    @NotBlank(message = "Contract title is required")
    private String title;

    private String description;

    private Long accountId;
    private Long opportunityId;
    private Long contactId;

    private String status;
    private String type;
    
    @NotNull(message = "Total amount is required")
    private BigDecimal totalAmount;

    private BigDecimal paidAmount;
    private BigDecimal taxAmount;
    
    private String paymentTerms;
    private String paymentDueDate;

    private String effectiveDate;
    private String expirationDate;
    private String signedDate;

    private Long ownerId;
    private String signatureData;
}
