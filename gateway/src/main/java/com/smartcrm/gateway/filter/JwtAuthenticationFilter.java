package com.smartcrm.gateway.filter;

import com.smartcrm.gateway.config.JwtAuthProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

/**
 * JWT authentication filter for Gateway.
 */
@Slf4j
@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private final JwtAuthProperties jwtAuthProperties;

    public JwtAuthenticationFilter(JwtAuthProperties jwtAuthProperties) {
        this.jwtAuthProperties = jwtAuthProperties;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        // Skip authentication for public paths
        if (isPublicPath(path)) {
            return chain.filter(exchange);
        }

        String token = extractToken(request);
        if (token == null) {
            return unauthorized(exchange, "Missing authentication token");
        }

        try {
            Claims claims = validateToken(token);
            // Add user info to headers for downstream services
            ServerHttpRequest modifiedRequest = request.mutate()
                    .header("X-User-Id", claims.getSubject())
                    .header("X-Tenant-Id", claims.get("tenantId", String.class))
                    .header("X-User-Roles", String.join(",", extractRoles(claims)))
                    .build();

            return chain.filter(exchange.mutate().request(modifiedRequest).build());
        } catch (ExpiredJwtException e) {
            log.warn("JWT token expired: {}", e.getMessage());
            return unauthorized(exchange, "Token expired");
        } catch (SignatureException e) {
            log.warn("Invalid JWT signature: {}", e.getMessage());
            return unauthorized(exchange, "Invalid token signature");
        } catch (MalformedJwtException e) {
            log.warn("Malformed JWT token: {}", e.getMessage());
            return unauthorized(exchange, "Malformed token");
        } catch (Exception e) {
            log.error("JWT validation failed: {}", e.getMessage());
            return unauthorized(exchange, "Token validation failed");
        }
    }

    private boolean isPublicPath(String path) {
        return path.startsWith("/actuator") ||
               path.startsWith("/auth") ||
               path.startsWith("/api/auth");
    }

    private String extractToken(ServerHttpRequest request) {
        String bearerToken = request.getHeaders().getFirst(jwtAuthProperties.getHeader());
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(jwtAuthProperties.getPrefix())) {
            return bearerToken.substring(jwtAuthProperties.getPrefix().length());
        }
        return null;
    }

    private Claims validateToken(String token) {
        SecretKey key = Keys.hmacShaKeyFor(jwtAuthProperties.getSecret().getBytes(StandardCharsets.UTF_8));
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    @SuppressWarnings("unchecked")
    private java.util.List<String> extractRoles(Claims claims) {
        Object roles = claims.get("roles");
        if (roles instanceof java.util.List) {
            return (java.util.List<String>) roles;
        }
        return java.util.Collections.emptyList();
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add("Content-Type", "application/json");
        String body = String.format("{\"code\":401,\"message\":\"%s\",\"timestamp\":\"%s\"}",
                message, java.time.LocalDateTime.now());
        return response.writeWith(Mono.just(response.bufferFactory().wrap(body.getBytes())));
    }

    @Override
    public int getOrder() {
        return -100;
    }
}