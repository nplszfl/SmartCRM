package com.smartcrm.ai.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartcrm.ai.config.AiServiceProperties;
import com.smartcrm.ai.exception.AiServiceException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.RetryBackoffSpec;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class LlmClientService {

    private final WebClient webClient;
    private final AiServiceProperties properties;
    private final ObjectMapper objectMapper;

    private static final String SYSTEM_PROMPT_LEAD_SCORING = """
            You are an expert B2B lead scoring analyst. Analyze the provided lead data and generate:
            1. A numerical score (0-100)
            2. A grade (A/B/C/D/F)
            3. Key scoring factors
            4. Recommendations for sales team

            Be precise and data-driven in your analysis.
            Return JSON only.
            """;

    private static final String SYSTEM_PROMPT_EMAIL_GENERATION = """
            You are an expert B2B sales copywriter. Generate personalized email content that:
            1. Is professional but conversational
            2. Creates urgency without being pushy
            3. Personalizes based on provided data
            4. Has clear CTAs
            5. Follows email best practices

            Return JSON with all fields filled in based on personalization data.
            """;

    private static final String SYSTEM_PROMPT_DEAL_PREDICTION = """
            You are an expert B2B sales analyst. Analyze deal data and predict:
            1. Close probability (0-100%)
            2. Risk level (low/medium/high)
            3. Key risk factors
            4. Recommendations to improve odds
            5. Predicted close date if available

            Be data-driven and specific in your analysis.
            Return JSON only.
            """;

    private static final String SYSTEM_PROMPT_CONVERSATION_ANALYSIS = """
            You are an expert B2B sales coach. Analyze sales conversation transcripts and provide:
            1. Overall sentiment assessment
            2. Key talking points identified
            3. Buying signals detected
            4. Objections raised
            5. Coaching insights for the sales rep
            6. Recommended next actions

            Be specific, actionable, and constructive in your feedback.
            Return JSON only.
            """;

    private static final String SYSTEM_PROMPT_INTELLIGENT_ROUTING = """
            You are an expert B2B sales team manager. Match leads to sales reps based on:
            1. Industry expertise alignment
            2. Deal size experience
            3. Geographic knowledge
            4. Product specialization
            5. Historical success with similar leads

            Provide detailed reasoning for the match and suggest alternatives.
            Return JSON only.
            """;

    @Retry(name = "deepseek")
    @CircuitBreaker(name = "deepseek", fallbackMethod = "completeFallback")
    public String complete(String prompt, String systemPrompt) {
        return complete(prompt, systemPrompt, Map.of())
                .block(Duration.ofSeconds(properties.getTimeoutSeconds()));
    }

    @Retry(name = "deepseek")
    @CircuitBreaker(name = "deepseek", fallbackMethod = "completeJsonFallback")
    public Mono<String> complete(String prompt, String systemPrompt, Map<String, Object> options) {
        String system = Optional.ofNullable(systemPrompt)
                .orElse("You are a helpful AI assistant.");

        List<Map<String, String>> messages = List.of(
                Map.of("role", "system", "content", system),
                Map.of("role", "user", "content", prompt)
        );

        double temperature = getOptionDouble(options, "temperature", 0.7);
        int maxTokens = getOptionInt(options, "maxTokens", 2000);

        Map<String, Object> payload = Map.of(
                "model", properties.getModel(),
                "messages", messages,
                "temperature", temperature,
                "max_tokens", maxTokens
        );

        log.debug("Sending request to DeepSeek API");

        return webClient.post()
                .uri("/chat/completions")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + properties.getApiKey())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(String.class)
                .doOnNext(response -> log.debug("Received response from DeepSeek API"))
                .doOnError(error -> log.error("DeepSeek API error: {}", error.getMessage()))
                .retryWhen(RetryBackoffSpec.backoff(3, Duration.ofSeconds(2))
                        .filter(this::isRetryableError));
    }

    public JsonNode completeJson(String prompt, String systemPrompt) {
        String response = complete(prompt, systemPrompt);
        return parseJsonResponse(response);
    }

    public JsonNode completeJson(String prompt, String systemPrompt, Map<String, Object> options) {
        String response = complete(prompt, systemPrompt, options)
                .block(Duration.ofSeconds(properties.getTimeoutSeconds()));
        return parseJsonResponse(response);
    }

    private JsonNode parseJsonResponse(String response) {
        try {
            JsonNode root = objectMapper.readTree(response);

            if (root.has("choices") && root.get("choices").isArray() && root.get("choices").size() > 0) {
                String content = root.get("choices").get(0).get("message").get("content").asText();
                String cleanedContent = cleanJsonString(content);
                return objectMapper.readTree(cleanedContent);
            }

            throw new AiServiceException("Invalid response format from LLM");
        } catch (JsonProcessingException e) {
            log.error("Failed to parse JSON response: {}", response, e);
            throw new AiServiceException("Failed to parse LLM response", e);
        }
    }

    private String cleanJsonString(String content) {
        String cleaned = content.trim();
        if (cleaned.startsWith("```json")) {
            cleaned = cleaned.substring(7);
        } else if (cleaned.startsWith("```")) {
            cleaned = cleaned.substring(3);
        }
        if (cleaned.endsWith("```")) {
            cleaned = cleaned.substring(0, cleaned.length() - 3);
        }
        return cleaned.trim();
    }

    private boolean isRetryableError(Throwable error) {
        if (error instanceof WebClientResponseException.ServiceUnavailable) {
            return true;
        }
        if (error instanceof WebClientResponseException.GatewayTimeout) {
            return true;
        }
        if (error instanceof java.net.ConnectException) {
            return true;
        }
        if (error instanceof java.net.SocketTimeoutException) {
            return true;
        }
        return false;
    }

    private double getOptionDouble(Map<String, Object> options, String key, double defaultValue) {
        if (options == null || !options.containsKey(key)) {
            return defaultValue;
        }
        Object value = options.get(key);
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        return defaultValue;
    }

    private int getOptionInt(Map<String, Object> options, String key, int defaultValue) {
        if (options == null || !options.containsKey(key)) {
            return defaultValue;
        }
        Object value = options.get(key);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return defaultValue;
    }

    @SuppressWarnings("unused")
    private String completeFallback(String prompt, String systemPrompt, Throwable throwable) {
        log.warn("Fallback triggered for prompt due to: {}", throwable.getMessage());
        throw new AiServiceException("AI service temporarily unavailable. Please try again later.", throwable);
    }

    @SuppressWarnings("unused")
    private Mono<String> completeJsonFallback(String prompt, String systemPrompt,
                                              Map<String, Object> options, Throwable throwable) {
        log.warn("Fallback triggered for JSON prompt due to: {}", throwable.getMessage());
        return Mono.error(new AiServiceException("AI service temporarily unavailable. Please try again later.", throwable));
    }

    public String getSystemPromptForType(String type) {
        return switch (type.toLowerCase()) {
            case "lead_scoring" -> SYSTEM_PROMPT_LEAD_SCORING;
            case "email_generation" -> SYSTEM_PROMPT_EMAIL_GENERATION;
            case "deal_prediction" -> SYSTEM_PROMPT_DEAL_PREDICTION;
            case "conversation_analysis" -> SYSTEM_PROMPT_CONVERSATION_ANALYSIS;
            case "intelligent_routing" -> SYSTEM_PROMPT_INTELLIGENT_ROUTING;
            default -> "You are a helpful AI assistant.";
        };
    }
}