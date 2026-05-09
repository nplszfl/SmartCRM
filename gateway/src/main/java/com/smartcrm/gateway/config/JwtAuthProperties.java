package com.smartcrm.gateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * JWT authentication properties.
 */
@Data
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtAuthProperties {

    private String secret;
    private Long expiration;
    private String header;
    private String prefix;
}