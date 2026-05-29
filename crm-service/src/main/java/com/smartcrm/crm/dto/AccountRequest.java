package com.smartcrm.crm.dto;

import lombok.Data;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

/**
 * Account request DTO with validation.
 */
@Data
public class AccountRequest {
    
    @NotBlank(message = "Account number is required")
    private String accountNumber;
    
    @NotBlank(message = "Account name is required")
    private String accountName;
    
    private String accountType;  // REVENUE, PREPAID, ACCUMULATED
    
    @DecimalMin(value = "0.0", message = "Balance cannot be negative")
    private BigDecimal balance;
    
    @DecimalMin(value = "0.0", message = "Credit limit cannot be negative")
    private BigDecimal creditLimit;
    
    private String currency;
    private String paymentTerms;
    private String billingAddress;
    private String shippingAddress;
    private String status;  // ACTIVE, SUSPENDED, CLOSED
    
    private Long ownerId;
    
    // Additional fields for customer linkage
    private Long customerId;
}