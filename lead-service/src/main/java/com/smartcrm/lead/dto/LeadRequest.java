package com.smartcrm.lead.dto;

import lombok.Data;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for creating/updating a lead
 */
@Data
public class LeadRequest {
    private Long id;

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @Email(message = "Invalid email format")
    private String email;

    private String phone;
    private String company;
    private String industry;
    private String companySize;
    private Double annualRevenue;
    private String title;
    private String source;

    private String status;
    private String rating;
    private Long ownerId;

    private String notes;
}
