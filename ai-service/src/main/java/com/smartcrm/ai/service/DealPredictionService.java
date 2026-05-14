package com.smartcrm.ai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.smartcrm.ai.dto.request.DealPredictRequest;
import com.smartcrm.ai.dto.response.DealPredictResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Deal Prediction Service
 * Uses AI to predict deal close probability, risk level, and recommendations
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DealPredictionService {

    private final LlmClientService llmClient;

    /**
     * Predict deal outcomes using AI analysis
     */
    public DealPredictResponse predictDeal(DealPredictRequest request) {
        log.info("Predicting deal: {}", request.getDealName());

        try {
            String prompt = buildPredictionPrompt(request);
            String systemPrompt = llmClient.getSystemPromptForType("deal_prediction");

            JsonNode result = llmClient.completeJson(prompt, systemPrompt);

            Double closeProbability = extractDouble(result, "close_probability", 50.0);
            String riskLevel = extractString(result, "risk_level", "medium");
            List<String> riskFactors = extractStringList(result, "risk_factors");
            List<String> recommendations = extractStringList(result, "recommendations");
            String predictedCloseDate = extractString(result, "predicted_close_date", 
                    calculateDefaultCloseDate(request.getStage()));

            return DealPredictResponse.builder()
                    .dealId(request.getDealId())
                    .closeProbability(closeProbability)
                    .riskLevel(riskLevel)
                    .riskFactors(riskFactors)
                    .recommendations(recommendations)
                    .predictedCloseDate(predictedCloseDate)
                    .build();

        } catch (Exception e) {
            log.error("Deal prediction error for {}: {}", request.getDealId(), e.getMessage());
            return fallbackPrediction(request);
        }
    }

    private String buildPredictionPrompt(DealPredictRequest request) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Analyze this B2B deal and predict its outcome.\n\n");
        prompt.append("DEAL INFORMATION:\n");
        prompt.append("- Deal ID: ").append(request.getDealId()).append("\n");
        prompt.append("- Deal Name: ").append(request.getDealName()).append("\n");
        prompt.append("- Account: ").append(request.getAccountName()).append("\n");
        prompt.append("- Current Stage: ").append(request.getStage()).append("\n");
        prompt.append("- Amount: $").append(request.getAmount()).append("\n");
        prompt.append("- Owner ID: ").append(request.getOwnerId()).append("\n");

        if (request.getProductLines() != null && !request.getProductLines().isEmpty()) {
            prompt.append("- Product Lines: ").append(String.join(", ", request.getProductLines())).append("\n");
        }

        if (request.getCompetitorInvolved() != null) {
            prompt.append("- Competitor Involved: ").append(request.getCompetitorInvolved()).append("\n");
        }

        if (request.getEngagementData() != null) {
            prompt.append("\nENGAGEMENT DATA:\n");
            DealPredictRequest.DealEngagementData eng = request.getEngagementData();
            prompt.append("- Meetings Held: ").append(nullSafe(eng.getMeetingsHeld())).append("\n");
            prompt.append("- Emails Exchanged: ").append(nullSafe(eng.getEmailsExchanged())).append("\n");
            prompt.append("- Demo Completed: ").append(nullSafe(eng.getDemoCompleted())).append("\n");
            prompt.append("- Proposal Sent: ").append(nullSafe(eng.getProposalSent())).append("\n");
            prompt.append("- Days Since Last Contact: ").append(nullSafe(eng.getDaysSinceLastContact())).append("\n");
        }

        if (request.getHistoricalWonDeals() != null && !request.getHistoricalWonDeals().isEmpty()) {
            prompt.append("\nHISTORICAL DEALS:\n");
            for (DealPredictRequest.HistoricalDeal deal : request.getHistoricalWonDeals()) {
                prompt.append("- ").append(deal.getName())
                        .append(": $").append(deal.getAmount())
                        .append(" (").append(deal.getWon() ? "WON" : "LOST").append(")\n");
            }
        }

        prompt.append("""
                
                Provide your prediction in JSON format:
                {
                    "close_probability": <0-100 percentage>,
                    "risk_level": "<low/medium/high>",
                    "risk_factors": ["<risk factor 1>", "<risk factor 2>"],
                    "recommendations": ["<recommendation 1>", "<recommendation 2>"],
                    "predicted_close_date": "<YYYY-MM-DD if available, otherwise null>"
                }
                
                Consider: deal stage progression, engagement levels, competitive landscape, and historical patterns.
                """);

        return prompt.toString();
    }

    private DealPredictResponse fallbackPrediction(DealPredictRequest request) {
        log.warn("Using rule-based fallback prediction for deal: {}", request.getDealId());

        double baseProbability = calculateBaseProbability(request.getStage());
        List<String> riskFactors = new ArrayList<>();
        List<String> recommendations = new ArrayList<>();

        // Adjust based on engagement data
        if (request.getEngagementData() != null) {
            DealPredictRequest.DealEngagementData eng = request.getEngagementData();
            
            if (eng.getDemoCompleted() != null && eng.getDemoCompleted()) {
                baseProbability += 10;
                recommendations.add("Demo completed - maintain engagement momentum");
            }
            
            if (eng.getProposalSent() != null && eng.getProposalSent()) {
                baseProbability += 15;
                recommendations.add("Proposal sent - follow up on feedback");
            }
            
            if (eng.getDaysSinceLastContact() != null && eng.getDaysSinceLastContact() > 7) {
                baseProbability -= 10;
                riskFactors.add("No contact in " + eng.getDaysSinceLastContact() + " days");
                recommendations.add("Reach out immediately to maintain relationship");
            }
            
            if (eng.getMeetingsHeld() != null && eng.getMeetingsHeld() < 2) {
                riskFactors.add("Limited meeting history");
            }
        }

        // Adjust for competitor involvement
        if (request.getCompetitorInvolved() != null && !request.getCompetitorInvolved().isEmpty()) {
            baseProbability -= 15;
            riskFactors.add("Competitor " + request.getCompetitorInvolved() + " is involved");
            recommendations.add("Highlight unique value propositions vs competitor");
        }

        // Determine risk level
        String riskLevel = baseProbability >= 70 ? "low" : (baseProbability >= 40 ? "medium" : "high");
        if (riskFactors.size() > 2) {
            riskLevel = "high";
        }

        // Generate recommendations if empty
        if (recommendations.isEmpty()) {
            recommendations.add("Continue nurturing the relationship");
            recommendations.add("Schedule follow-up meeting to advance stage");
        }

        return DealPredictResponse.builder()
                .dealId(request.getDealId())
                .closeProbability(Math.max(0, Math.min(100, baseProbability)))
                .riskLevel(riskLevel)
                .riskFactors(riskFactors.isEmpty() ? List.of("Standard deal progression") : riskFactors)
                .recommendations(recommendations)
                .predictedCloseDate(calculateDefaultCloseDate(request.getStage()))
                .build();
    }

    private double calculateBaseProbability(String stage) {
        return switch (stage.toUpperCase()) {
            case "PROSPECTING" -> 20.0;
            case "QUALIFICATION" -> 35.0;
            case "PROPOSAL" -> 55.0;
            case "NEGOTIATION" -> 75.0;
            case "CLOSED_WON" -> 100.0;
            case "CLOSED_LOST" -> 0.0;
            default -> 25.0;
        };
    }

    private String calculateDefaultCloseDate(String stage) {
        int daysToAdd = switch (stage.toUpperCase()) {
            case "PROSPECTING" -> 90;
            case "QUALIFICATION" -> 60;
            case "PROPOSAL" -> 45;
            case "NEGOTIATION" -> 30;
            default -> 60;
        };
        return LocalDate.now().plusDays(daysToAdd).toString();
    }

    private Double extractDouble(JsonNode node, String field, Double defaultValue) {
        if (node.has(field) && !node.get(field).isNull()) {
            return node.get(field).asDouble(defaultValue);
        }
        return defaultValue;
    }

    private String extractString(JsonNode node, String field, String defaultValue) {
        if (node.has(field) && !node.get(field).isNull()) {
            return node.get(field).asText(defaultValue);
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
