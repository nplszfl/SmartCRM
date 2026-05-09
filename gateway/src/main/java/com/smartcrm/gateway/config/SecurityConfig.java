package com.smartcrm.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

/**
 * Security configuration for the Gateway.
 */
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    private final JwtAuthProperties jwtAuthProperties;

    public SecurityConfig(JwtAuthProperties jwtAuthProperties) {
        this.jwtAuthProperties = jwtAuthProperties;
    }

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers("/actuator/**", "/auth/**").permitAll()
                        .anyExchange().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
                )
                .build();
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        byte[] keyBytes = jwtAuthProperties.getSecret().getBytes(StandardCharsets.UTF_8);
        SecretKey key = new SecretKeySpec(keyBytes, "HmacSHA256");
        return NimbusJwtDecoder.withSecretKey(key).build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(new JwtAuthorityConverter());
        return converter;
    }

    static class JwtAuthorityConverter implements Converter<Jwt, java.util.List<org.springframework.security.core.GrantedAuthority>> {
        @Override
        public java.util.List<org.springframework.security.core.GrantedAuthority> convert(Jwt jwt) {
            java.util.List<org.springframework.security.core.GrantedAuthority> authorities = new java.util.ArrayList<>();

            // Extract roles from JWT
            Object rolesObj = jwt.getClaim("roles");
            if (rolesObj instanceof java.util.List) {
                @SuppressWarnings("unchecked")
                java.util.List<String> roles = (java.util.List<String>) rolesObj;
                roles.forEach(role -> authorities.add(
                    new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_" + role)
                ));
            }

            // Extract permissions
            Object permissionsObj = jwt.getClaim("permissions");
            if (permissionsObj instanceof java.util.List) {
                @SuppressWarnings("unchecked")
                java.util.List<String> permissions = (java.util.List<String>) permissionsObj;
                permissions.forEach(perm -> authorities.add(
                    new org.springframework.security.core.authority.SimpleGrantedAuthority(perm)
                ));
            }

            return authorities;
        }
    }
}