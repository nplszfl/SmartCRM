package com.smartcrm.email.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * EmailTemplate entity - represents an email template in the system
 */
@Data
@TableName("email_templates")
public class EmailTemplate {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;
    private String subject;
    private String body;
    private String htmlBody;
    private String type; // OUTBOUND, INBOUND
    private String description;
    private Boolean active;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}