package com.smartcrm.crm.entity;

import com.smartcrm.common.entity.UserEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;

/**
 * Customer entity.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("customers")
public class Customer extends UserEntity {

    private String name;
    private String industry;
    private String website;
    private String phone;
    private String email;
    private String address;
    private String city;
    private String state;
    private String country;
    private String postalCode;
    private BigDecimal annualRevenue;
    private Integer employeeCount;
    private String customerType;  // PROSPECT, ACTIVE, INACTIVE, CHURNED
    private String customerSource;
    private Long ownerId;
}