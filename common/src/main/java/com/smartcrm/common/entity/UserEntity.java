package com.smartcrm.common.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Base user entity with organization and tenant support.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public abstract class UserEntity extends BaseEntity {

    private Long userId;
    private Long organizationId;
    private String tenantId;
}