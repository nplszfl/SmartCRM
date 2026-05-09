package com.smartcrm.crm.entity;

import com.smartcrm.common.entity.UserEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;

/**
 * Account entity.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("accounts")
public class Account extends UserEntity {

    private Long customerId;
    private String accountNumber;
    private String accountName;
    private String accountType;  // REVENUE, PREPAID, ACCUMULATED
    private BigDecimal balance;
    private BigDecimal creditLimit;
    private String currency;
    private String paymentTerms;
    private String billingAddress;
    private String shippingAddress;
    private String status;  // ACTIVE, SUSPENDED, CLOSED
    private Long ownerId;
}