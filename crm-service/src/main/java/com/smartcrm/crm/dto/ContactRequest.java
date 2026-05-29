package com.smartcrm.crm.dto;

import lombok.Data;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

/**
 * Contact request DTO with validation.
 */
@Data
public class ContactRequest {
    
    private Long customerId;
    
    @NotBlank(message = "First name is required")
    private String firstName;
    
    @NotBlank(message = "Last name is required")
    private String lastName;
    
    @Email(message = "Invalid email format")
    private String email;
    
    private String phone;
    private String mobile;
    private String jobTitle;
    private String department;
    private Boolean isPrimary;
    private String linkedinUrl;
    private String twitterHandle;
    private String facebookUrl;
    private LocalDate dateOfBirth;
    private String notes;
    private Long ownerId;
}