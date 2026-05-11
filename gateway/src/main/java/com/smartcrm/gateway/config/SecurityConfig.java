package com.smartcrm.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
    public NimbusJwtDecoder jwtDecoder() {
        byte[] keyBytes = jwtAuthProperties.getSecret().getBytes(StandardCharsets.UTF_8);
        SecretKey key = new SecretKeySpec(keyBytes, "HmacSHA256");
        return NimbusJwtDecoder.withSecretKey(key).build();
    }

    @Bean
    public ReactiveJwtAuthenticationConverterAdapter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(new JwtAuthorityConverter());
        return new ReactiveJwtAuthenticationConverterAdapter(converter);
    }

    static class JwtAuthorityConverter implements Converter<Jwt, Collection<GrantedAuthority>> {
        @Override
        public Collection<GrantedAuthority> convert(Jwt jwt) {
            List<GrantedAuthority> authorities = new ArrayList<>();

            // Extract roles from JWT
            Object rolesObj = jwt.getClaim("roles");
            if (rolesObj instanceof List) {
                @SuppressWarnings("unchecked")
                List<String> roles = (List<String>) rolesObj;
                roles.forEach(role -> authorities.add(
                    new SimpleGrantedAuthority("ROLE_" + role)
                ));
            }

            // Extract permissions
            Object permissionsObj = jwt.getClaim("permissions");
            if (permissionsObj instanceof List) {
                @SuppressWarnings("unchecked")
                List<String> permissions = (List<String>) permissionsObj;
                permissions.forEach(perm -> authorities.add(
                    new SimpleGrantedAuthority(perm)
                ));
            }

            return authorities;
        }
    }
}
