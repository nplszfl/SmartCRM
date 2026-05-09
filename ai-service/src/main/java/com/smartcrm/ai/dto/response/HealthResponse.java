package com.smartcrm.ai.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HealthResponse {
    private String status;
    private String service;
    private String version;
    private LocalDateTime timestamp;
    private AiProviderStatus aiProvider;
    private CacheStatus cache;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AiProviderStatus {
        private String provider;
        private String status;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CacheStatus {
        private String status;
    }
}