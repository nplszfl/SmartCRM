package com.smartcrm.email.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for creating an email template
 */
@Data
public class EmailTemplateRequest {
    private Long id;
    
    @NotBlank(message = "Template name is required")
    private String name;
    
    @NotBlank(message = "Subject template is required")
    private String subject;
    
    @NotBlank(message = "Body template is required")
    private String body;
    
    private String htmlBody;
    
    private String type; // OUTBOUND, INBOUND
    
    private String description;
    
    private Boolean active = true;
}