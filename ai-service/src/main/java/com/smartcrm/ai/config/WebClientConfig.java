package com.smartcrm.ai.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class WebClientConfig {

    private final AiServiceProperties aiServiceProperties;

    @Bean
    public WebClient webClient(WebClient.Builder builder) {
        return builder
                .baseUrl(aiServiceProperties.getBaseUrl())
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    @Bean
    public Retry deepseekRetry(RetryRegistry registry) {
        Retry retry = registry.retry("deepseek");
        retry.getEventPublisher()
                .onRetry(event -> log.warn("Retrying DeepSeek API call, attempt {}",
                        event.getNumberOfRetryAttempts()));
        return retry;
    }

    @Bean
    public CircuitBreaker deepseekCircuitBreaker(CircuitBreakerRegistry registry) {
        CircuitBreaker circuitBreaker = registry.circuitBreaker("deepseek");
        circuitBreaker.getEventPublisher()
                .onStateTransition(event -> log.warn("DeepSeek circuit breaker state changed: {}",
                        event.getStateTransition()));
        return circuitBreaker;
    }
}