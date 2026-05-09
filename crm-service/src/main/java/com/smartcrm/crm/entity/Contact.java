package com.smartcrm.crm.entity;

import com.smartcrm.common.entity.UserEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDate;

/**
 * Contact entity.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("contacts")
public class Contact extends UserEntity {

    private Long customerId;
    private String firstName;
    private String lastName;
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