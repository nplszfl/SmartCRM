package com.smartcrm.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Request ID filter for tracing.
 */
@Slf4j
@Component
public class RequestIdFilter implements GlobalFilter, Ordered {

    private static final String REQUEST_ID_HEADER = "X-Request-Id";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String requestId = exchange.getRequest().getHeaders().getFirst(REQUEST_ID_HEADER);
        if (requestId == null) {
            requestId = UUID.randomUUID().toString();
        }

        exchange.getResponse().getHeaders().add(REQUEST_ID_HEADER, requestId);

        return chain.filter(exchange.mutate()
                .request(exchange.getRequest().mutate()
                        .header(REQUEST_ID_HEADER, requestId)
                        .build())
                .build());
    }

    @Override
    public int getOrder() {
        return -90;
    }
}