package com.smartcrm.ai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.smartcrm.ai.dto.request.RepMatchRequest;
import com.smartcrm.ai.dto.response.RepMatchResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Rep Match Service
 * Intelligently matches leads to sales reps based on expertise, experience, and workload
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RepMatchService {

    private final LlmClientService llmClient;

    // Simulated sales rep profiles (in production, would fetch from database)
    private static final List<Map<String, Object>> SALES_REPS = List.of(
            Map.of("id", "1", "name", "John Smith", "industries", List.of("Technology", "SaaS"), 
                    "dealSizes", List.of("Enterprise", "Mid-Market"), "regions", List.of("North America")),
            Map.of("id", "2", "name", "Sarah Johnson", "industries", List.of("Healthcare", "Finance"), 
                    "dealSizes", List.of("Enterprise"), "regions", List.of("North America", "Europe")),
            Map.of("id", "3", "name", "Mike Chen", "industries", List.of("Technology", "Manufacturing"), 
                    "dealSizes", List.of("SMB", "Mid-Market"), "regions", List.of("Asia Pacific")),
            Map.of("id", "4", "name", "Emily Davis", "industries", List.of("Retail", "Healthcare"), 
                    "dealSizes", List.of("SMB", "Mid-Market"), "regions", List.of("North America"))
    );

    /**
     * Match a lead to the best sales rep
     */
    public RepMatchResponse matchRep(RepMatchRequest request) {
        log.info("Matching rep for lead: {}", request.getLeadId());

        try {
            String prompt = buildMatchPrompt(request);
            String systemPrompt = llmClient.getSystemPromptForType("intelligent_routing");

            JsonNode result = llmClient.completeJson(prompt, systemPrompt);

            String matchedRepId = extractString(result, "matched_rep_id", "1");
            String matchedRepName = extractString(result, "matched_rep_name", "John Smith");
            List<String> matchReasons = extractStringList(result, "match_reasons");
            List<String> alternativeRepNames = extractStringList(result, "alternative_reps");
            Double matchScore = extractDouble(result, "confidence_score", 0.7);

            // Build alternative reps list
            List<RepMatchResponse.AlternativeRep> alternatives = new ArrayList<>();
            for (int i = 0; i < alternativeRepNames.size() && i < 2; i++) {
                alternatives.add(RepMatchResponse.AlternativeRep.builder()
                        .repName(alternativeRepNames.get(i))
                        .score(0.8 - (i * 0.2))
                        .build());
            }

            return RepMatchResponse.builder()
                    .leadId(request.getLeadId())
                    .matchedRepId(matchedRepId)
                    .matchedRepName(matchedRepName)
                    .matchScore(matchScore)
                    .alternativeReps(alternatives)
                    .reasoning(String.join("; ", matchReasons))
                    .build();

        } catch (Exception e) {
            log.error("Rep match error for {}: {}", request.getLeadId(), e.getMessage());
            return fallbackMatch(request);
        }
    }

    private String buildMatchPrompt(RepMatchRequest request) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Match this lead to the best sales rep from our team.\n\n");
        prompt.append("LEAD INFORMATION:\n");
        prompt.append("- Lead ID: ").append(request.getLeadId()).append("\n");
        prompt.append("- Company: ").append(request.getCompanyName()).append("\n");
        prompt.append("- Industry: ").append(request.getIndustry()).append("\n");
        prompt.append("- Deal Size: ").append(request.getDealSize()).append("\n");
        prompt.append("- Geographic Preference: ").append(nullSafe(request.getGeographicPreference())).append("\n");
        prompt.append("- Product Interest: ").append(nullSafe(request.getProductInterest())).append("\n");
        prompt.append("- Required Skills: ").append(request.getRequiredSkills() != null ? 
                String.join(", ", request.getRequiredSkills()) : "None specified").append("\n");

        prompt.append("\n\nAVAILABLE SALES REPS:\n");
        for (Map<String, Object> rep : SALES_REPS) {
            prompt.append("- ID: ").append(rep.get("id")).append("\n");
            prompt.append("  Name: ").append(rep.get("name")).append("\n");
            prompt.append("  Industries: ").append(String.join(", ", (List<String>) rep.get("industries"))).append("\n");
            prompt.append("  Deal Sizes: ").append(String.join(", ", (List<String>) rep.get("dealSizes"))).append("\n");
            prompt.append("  Regions: ").append(String.join(", ", (List<String>) rep.get("regions"))).append("\n\n");
        }

        prompt.append("""
                
                Provide your match recommendation in JSON format:
                {
                    "matched_rep_id": "<1-4 as string>",
                    "matched_rep_name": "<name>",
                    "match_reasons": ["<reason 1>", "<reason 2>", "<reason 3>"],
                    "alternative_reps": ["<rep name 1>", "<rep name 2>"],
                    "confidence_score": <0.0-1.0>
                }
                
                Consider: industry expertise alignment, deal size experience, geographic knowledge, and product specialization.
                """);

        return prompt.toString();
    }

    private RepMatchResponse fallbackMatch(RepMatchRequest request) {
        log.warn("Using rule-based fallback match for lead: {}", request.getLeadId());

        // Rule-based matching as fallback
        String industry = request.getIndustry().toLowerCase();
        String dealSize = request.getDealSize().toLowerCase();
        
        String matchedRepId = "1";
        String matchedRepName = "John Smith";
        List<String> matchReasons = new ArrayList<>();
        
        // Match by industry
        if (industry.contains("health") || industry.contains("pharma") || industry.contains("medical")) {
            matchedRepId = "2";
            matchedRepName = "Sarah Johnson";
            matchReasons.add("Healthcare industry expertise");
        } else if (industry.contains("tech") || industry.contains("software") || industry.contains("saas")) {
            matchedRepId = "1";
            matchedRepName = "John Smith";
            matchReasons.add("Technology/SaaS specialization");
        } else if (industry.contains("manufacturing") || industry.contains("industrial")) {
            matchedRepId = "3";
            matchedRepName = "Mike Chen";
            matchReasons.add("Manufacturing sector experience");
        } else if (industry.contains("retail") || industry.contains("ecommerce")) {
            matchedRepId = "4";
            matchedRepName = "Emily Davis";
            matchReasons.add("Retail industry focus");
        } else {
            matchReasons.add("General assignment based on availability");
        }
        
        // Match by deal size
        if (dealSize.contains("enterprise") || dealSize.contains("large")) {
            matchReasons.add("Enterprise deal experience");
        } else if (dealSize.contains("smb") || dealSize.contains("small")) {
            matchReasons.add("SMB deal specialization");
        } else {
            matchReasons.add("Mid-market deal handling");
        }
        
        // Match by geographic preference
        if (request.getGeographicPreference() != null) {
            String region = request.getGeographicPreference().toLowerCase();
            if (region.contains("europe")) {
                matchedRepId = "2";
                matchedRepName = "Sarah Johnson";
                matchReasons.add("Europe region coverage");
            } else if (region.contains("asia") || region.contains("pacific")) {
                matchedRepId = "3";
                matchedRepName = "Mike Chen";
                matchReasons.add("APAC region expertise");
            }
        }
        
        // Generate alternatives
        final String finalMatchedRepId = matchedRepId;
        List<RepMatchResponse.AlternativeRep> alternatives = SALES_REPS.stream()
                .filter(r -> !r.get("id").equals(finalMatchedRepId))
                .map(r -> RepMatchResponse.AlternativeRep.builder()
                        .repId((String) r.get("id"))
                        .repName((String) r.get("name"))
                        .score(0.65)
                        .build())
                .limit(2)
                .toList();
        
        return RepMatchResponse.builder()
                .leadId(request.getLeadId())
                .matchedRepId(matchedRepId)
                .matchedRepName(matchedRepName)
                .matchScore(0.65) // Lower confidence for rule-based
                .alternativeReps(alternatives)
                .reasoning(String.join("; ", matchReasons))
                .build();
    }

    private String extractString(JsonNode node, String field, String defaultValue) {
        if (node.has(field) && !node.get(field).isNull()) {
            return node.get(field).asText(defaultValue);
        }
        return defaultValue;
    }

    private Double extractDouble(JsonNode node, String field, Double defaultValue) {
        if (node.has(field) && !node.get(field).isNull()) {
            return node.get(field).asDouble(defaultValue);
        }
        return defaultValue;
    }

    private List<String> extractStringList(JsonNode node, String field) {
        List<String> result = new ArrayList<>();
        if (node.has(field) && node.get(field).isArray()) {
            node.get(field).forEach(item -> result.add(item.asText()));
        }
        return result;
    }

    private String nullSafe(Object value) {
        return value != null ? value.toString() : "N/A";
    }
}
