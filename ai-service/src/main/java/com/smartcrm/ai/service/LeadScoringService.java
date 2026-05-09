package com.smartcrm.ai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.smartcrm.ai.dto.request.LeadScoreRequest;
import com.smartcrm.ai.dto.response.LeadScoreResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class LeadScoringService {

    private final LlmClientService llmClient;

    private static final Map<String, int[]> GRADE_THRESHOLDS = Map.of(
            "A", new int[]{80, 100},
            "B", new int[]{60, 79},
            "C", new int[]{40, 59},
            "D", new int[]{20, 39},
            "F", new int[]{0, 19}
    );

    public LeadScoreResponse scoreLead(LeadScoreRequest request) {
        log.info("Scoring lead: {}", request.getLeadId());

        try {
            String prompt = buildAnalysisPrompt(request);
            String systemPrompt = llmClient.getSystemPromptForType("lead_scoring");

            JsonNode result = llmClient.completeJson(prompt, systemPrompt);

            int score = result.has("score") ? result.get("score").asInt(50) : 50;
            String grade = calculateGrade(score);

            List<String> keyFactors = extractStringList(result, "key_factors");
            List<String> recommendations = extractStringList(result, "recommendations");
            String reasoning = result.has("reasoning") ? result.get("reasoning").asText("Analysis completed.") : "Analysis completed.";

            return LeadScoreResponse.builder()
                    .leadId(request.getLeadId())
                    .score(score)
                    .grade(grade)
                    .reasoning(reasoning)
                    .keyFactors(keyFactors)
                    .recommendations(recommendations)
                    .build();

        } catch (Exception e) {
            log.error("Lead scoring error for {}: {}", request.getLeadId(), e.getMessage());
            return fallbackScore(request);
        }
    }

    private String buildAnalysisPrompt(LeadScoreRequest request) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Analyze this B2B lead and provide a comprehensive scoring assessment.\n\n");
        prompt.append("LEAD INFORMATION:\n");
        prompt.append("- Lead ID: ").append(request.getLeadId()).append("\n");
        prompt.append("- Contact Name: ").append(request.getContactName()).append("\n");
        prompt.append("- Contact Title: ").append(nullSafe(request.getContactTitle())).append("\n");
        prompt.append("- Company Name: ").append(request.getCompanyName()).append("\n");
        prompt.append("- Industry: ").append(nullSafe(request.getIndustry())).append("\n");
        prompt.append("- Company Size: ").append(nullSafe(request.getCompanySize())).append("\n");
        prompt.append("- Revenue: ").append(nullSafe(request.getRevenue())).append("\n");
        prompt.append("- Source: ").append(nullSafe(request.getSource())).append("\n");

        if (request.getBehavioralData() != null) {
            prompt.append("\nBEHAVIORAL DATA:\n");
            LeadScoreRequest.LeadBehavioralData bd = request.getBehavioralData();
            prompt.append("- Emails Opened: ").append(nullSafe(bd.getEmailsOpened())).append("\n");
            prompt.append("- Pages Viewed: ").append(nullSafe(bd.getPagesViewed())).append("\n");
            prompt.append("- Events Attended: ").append(nullSafe(bd.getEventsAttended())).append("\n");
            prompt.append("- Webinars Attended: ").append(nullSafe(bd.getWebinarsAttended())).append("\n");
            prompt.append("- Downloads: ").append(nullSafe(bd.getDownloads())).append("\n");
        }

        if (request.getFirmographicData() != null) {
            prompt.append("\nFIRMOGRAPHIC DATA:\n");
            LeadScoreRequest.LeadFirmographicData fd = request.getFirmographicData();
            prompt.append("- Employee Count: ").append(nullSafe(fd.getEmployeeCount())).append("\n");
            prompt.append("- Annual Revenue: ").append(nullSafe(fd.getAnnualRevenue())).append("\n");
            prompt.append("- Market Segment: ").append(nullSafe(fd.getMarketSegment())).append("\n");
            prompt.append("- Tech Stack: ").append(nullSafe(fd.getTechStack())).append("\n");
        }

        prompt.append("""
                Provide your analysis in JSON format:
                {
                    "score": <0-100 integer>,
                    "reasoning": "<2-3 sentence explanation>",
                    "key_factors": ["<factor 1>", "<factor 2>", "<factor 3>"],
                    "recommendations": ["<recommendation 1>", "<recommendation 2>"]
                }

                Focus on: title seniority, company fit, engagement level, and buying signals.
                """);

        return prompt.toString();
    }

    private String calculateGrade(int score) {
        for (Map.Entry<String, int[]> entry : GRADE_THRESHOLDS.entrySet()) {
            int[] range = entry.getValue();
            if (score >= range[0] && score <= range[1]) {
                return entry.getKey();
            }
        }
        return "C";
    }

    private LeadScoreResponse fallbackScore(LeadScoreRequest request) {
        log.warn("Using rule-based fallback scoring for lead: {}", request.getLeadId());

        int score = 50;
        List<String> factors = new ArrayList<>();
        List<String> recommendations = new ArrayList<>();

        if (request.getContactTitle() != null) {
            String title = request.getContactTitle().toLowerCase();
            if (title.contains("ceo") || title.contains("cto") || title.contains("cfo") ||
                title.contains("vp") || title.contains("director") || title.contains("head")) {
                score += 15;
                factors.add("Senior-level contact");
            }
        }

        if (request.getCompanySize() != null) {
            String size = request.getCompanySize().toLowerCase();
            if (size.contains("enterprise") || size.contains("1000") || size.contains("5000")) {
                score += 20;
                factors.add("Large enterprise company");
            } else if (size.contains("mid") || size.contains("500")) {
                score += 10;
                factors.add("Mid-market company");
            }
        }

        if (request.getIndustry() != null) {
            score += 5;
            factors.add("Valid industry: " + request.getIndustry());
        }

        score = Math.min(score, 100);
        String grade = calculateGrade(score);

        return LeadScoreResponse.builder()
                .leadId(request.getLeadId())
                .score(score)
                .grade(grade)
                .reasoning("Score calculated using rule-based fallback due to LLM unavailable.")
                .keyFactors(factors.isEmpty() ? List.of("Basic information provided") : factors)
                .recommendations(List.of("Review lead manually for complete assessment"))
                .build();
    }

    private List<String> extractStringList(JsonNode node, String field) {
        List<String> result = new ArrayList<>();
        if (node.has(field) && node.get(field).isArray()) {
            node.get(field).forEach(item -> result.add(item.asText()));
        }
        return result;
    }

    private String nullSafe(Object value) {
        return value != null ? value.toString() : "Unknown";
    }
}