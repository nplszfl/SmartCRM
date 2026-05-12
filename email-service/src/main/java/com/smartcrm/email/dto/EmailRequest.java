package com.smartcrm.email.dto;

import lombok.Data;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for creating/sending an email
 */
@Data
public class EmailRequest {
    private Long id;
    
    private Long leadId;
    private Long contactId;
    private Long opportunityId;

    @NotBlank(message = "Recipient is required")
    @Email(message = "Invalid email format")
    private String toAddress;

    private String ccAddress;
    private String bccAddress;

    @NotBlank(message = "Subject is required")
    private String subject;

    @NotBlank(message = "Body is required")
    private String body;

    private String htmlBody;
    private String type; // OUTBOUND, INBOUND
    private String templateId;
    private String aiPromptUsed;
}