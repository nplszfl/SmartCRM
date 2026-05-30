package com.smartcrm.email.dto;

import lombok.Data;
import lombok.Builder;
import java.time.LocalDateTime;

/**
 * Response DTO for email template operations
 */
@Data
@Builder
public class EmailTemplateResponse {
    private Long id;
    private String name;
    private String subject;
    private String body;
    private String htmlBody;
    private String type;
    private String description;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}