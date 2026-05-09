package com.smartcrm.common.ai;

import com.smartcrm.common.dto.AIInsightResult;
import com.smartcrm.common.dto.AIPredictionResult;
import com.smartcrm.common.dto.AIScoreResult;
import lombok.Data;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.ChatResponse;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * AI Client for SmartCRM - handles all AI/ML interactions.
 * Supports lead scoring, deal prediction, email generation, and analytics insights.
 */
@Slf4j
@Component
public class AIClient {

    private final ChatClient chatClient;

    public AIClient(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    /**
     * Score a lead using AI analysis.
     */
    public AIScoreResult scoreLead(LeadScoringContext context) {
        log.info("Scoring lead: {} with company: {}", context.getLeadName(), context.getCompanyName());

        String prompt = buildLeadScoringPrompt(context);
        String reasoning = callAI(prompt);

        // Parse AI response to extract score and reasoning
        Double score = extractScoreFromReasoning(reasoning);
        String confidence = determineConfidence(score);

        return AIScoreResult.builder()
                .score(score)
                .reasoning(reasoning)
                .confidence(confidence)
                .factors(extractKeyFactors(context))
                .recommendedAction(determineRecommendedAction(score, context))
                .build();
    }

    /**
     * Predict deal outcome using AI.
     */
    public AIPredictionResult predictDeal(DealPredictionContext context) {
        log.info("Predicting deal: {} with value: {}", context.getDealName(), context.getDealValue());

        String prompt = buildDealPredictionPrompt(context);
        String reasoning = callAI(prompt);

        Double probability = extractProbabilityFromReasoning(reasoning);
        String[] riskFactors = extractRiskFactors(reasoning, context);

        return AIPredictionResult.builder()
                .probability(probability)
                .prediction(probability >= 0.5 ? "CLOSE_WON" : "CLOSED_LOST")
                .reasoning(reasoning)
                .riskFactors(riskFactors)
                .recommendedNextAction(suggestNextAction(context, riskFactors))
                .predictedCloseDate(estimateCloseDate(context))
                .expectedValue(context.getDealValue() * probability)
                .build();
    }

    /**
     * Generate email content using AI.
     */
    public String generateEmail(EmailGenerationContext context) {
        log.info("Generating email for: {}", context.getRecipientName());

        String prompt = buildEmailGenerationPrompt(context);
        return callAI(prompt);
    }

    /**
     * Generate analytics insights using AI.
     */
    public AIInsightResult generateInsight(AnalyticsInsightContext context) {
        log.info("Generating {} insight for period: {}", context.getInsightType(), context.getPeriod());

        String prompt = buildAnalyticsInsightPrompt(context);
        String response = callAI(prompt);

        return AIInsightResult.builder()
                .insightType(context.getInsightType())
                .title(extractInsightTitle(response))
                .description(response)
                .confidence(0.85)
                .recommendations(extractRecommendations(response))
                .data(context.getData())
                .build();
    }

    private String callAI(String prompt) {
        try {
            Prompt aiPrompt = new Prompt(
                new UserMessage(prompt),
                OpenAiChatOptions.builder()
                    .withModel("gpt-4-turbo")
                    .withTemperature(0.7)
                    .build()
            );

            ChatResponse response = chatClient.call(aiPrompt);
            return response.getResult().getOutput().getContent();
        } catch (Exception e) {
            log.error("AI call failed: {}", e.getMessage());
            return "AI analysis unavailable. Please try again.";
        }
    }

    private String buildLeadScoringPrompt(LeadScoringContext context) {
        return String.format("""
            Analyze the following lead and provide a score from 0-100 with detailed reasoning.

            Lead Information:
            - Name: %s
            - Company: %s
            - Industry: %s
            - Company Size: %s
            - Source: %s
            - Job Title: %s
            - Email Domain: %s
            - Behavior Score: %d (pages visited, emails opened, etc.)

            Scoring Criteria:
            1. Company fit (size, industry match)
            2. Decision maker potential (job title)
            3. Engagement level (behavior data)
            4. Source quality (referral > organic > cold)
            5. Budget fit

            Provide your response in JSON format:
            {
              "score": <0-100>,
              "reasoning": "<detailed explanation>",
              "key_factors": ["factor1", "factor2", "factor3"],
              "recommended_action": "<next best action>"
            }
            """,
            context.getLeadName(),
            context.getCompanyName(),
            context.getIndustry(),
            context.getCompanySize(),
            context.getSource(),
            context.getJobTitle(),
            context.getEmailDomain(),
            context.getBehaviorScore()
        );
    }

    private String buildDealPredictionPrompt(DealPredictionContext context) {
        return String.format("""
            Predict the outcome of this deal and identify risk factors.

            Deal Information:
            - Name: %s
            - Value: $%.2f
            - Stage: %s
            - Days in current stage: %d
            - Last contact date: %s
            - Competitors involved: %s
            - Decision makers: %d
            - Proposal sent: %s

            Historical Context:
            - Win rate for similar deals: %.1f%%
            - Average deal cycle: %d days

            Provide your response in JSON format:
            {
              "probability": <0-1>,
              "prediction": "CLOSE_WON" or "CLOSED_LOST",
              "reasoning": "<detailed explanation>",
              "risk_factors": ["risk1", "risk2"],
              "recommended_next_action": "<action>",
              "estimated_close_date": "<YYYY-MM-DD>"
            }
            """,
            context.getDealName(),
            context.getDealValue(),
            context.getStage(),
            context.getDaysInStage(),
            context.getLastContactDate(),
            context.getCompetitors(),
            context.getDecisionMakers(),
            context.isProposalSent() ? "Yes" : "No",
            context.getHistoricalWinRate(),
            context.getAverageDealCycle()
        );
    }

    private String buildEmailGenerationPrompt(EmailGenerationContext context) {
        return String.format("""
            Generate a personalized email for a sales outreach.

            Recipient Information:
            - Name: %s
            - Company: %s
            - Job Title: %s

            Email Purpose: %s
            - Tone: %s
            - Key points to include: %s

            Context: %s

            Generate a compelling, professional email that:
            1. Has an engaging subject line
            2. Personalizes the opening
            3. Creates value
            4. Has a clear call to action

            Return ONLY the email content (subject line + body).
            """,
            context.getRecipientName(),
            context.getRecipientCompany(),
            context.getRecipientJobTitle(),
            context.getPurpose(),
            context.getTone(),
            String.join(", ", context.getKeyPoints()),
            context.getContext()
        );
    }

    private String buildAnalyticsInsightPrompt(AnalyticsInsightContext context) {
        return String.format("""
            Analyze the following sales data and provide actionable insights.

            Period: %s
            Data Summary: %s

            Key Metrics:
            - Total Revenue: $%.2f
            - Deals Won: %d
            - Deals Lost: %d
            - Average Deal Size: $%.2f
            - Win Rate: %.1f%%
            - Pipeline Value: $%.2f

            Generate insights in JSON format:
            {
              "title": "<insight title>",
              "description": "<detailed analysis>",
              "key_findings": ["finding1", "finding2"],
              "recommendations": ["action1", "action2"],
              "anomalies": ["if any"]
            }
            """,
            context.getPeriod(),
            context.getSummary(),
            context.getTotalRevenue(),
            context.getDealsWon(),
            context.getDealsLost(),
            context.getAverageDealSize(),
            context.getWinRate(),
            context.getPipelineValue()
        );
    }

    // Helper methods for parsing AI responses
    private Double extractScoreFromReasoning(String reasoning) {
        // Simple extraction - in production, use proper JSON parsing
        if (reasoning.contains("\"score\":")) {
            String[] parts = reasoning.split("\"score\":");
            if (parts.length > 1) {
                String numStr = parts[1].trim().split("[,\\}]")[0];
                try {
                    return Double.parseDouble(numStr);
                } catch (NumberFormatException e) {
                    return 50.0; // Default
                }
            }
        }
        return 50.0;
    }

    private String determineConfidence(Double score) {
        if (score >= 80 || score <= 20) return "HIGH";
        if (score >= 40 || score <= 60) return "MEDIUM";
        return "LOW";
    }

    private Double extractProbabilityFromReasoning(String reasoning) {
        if (reasoning.contains("\"probability\":")) {
            String[] parts = reasoning.split("\"probability\":");
            if (parts.length > 1) {
                String numStr = parts[1].trim().split("[,\\}]")[0];
                try {
                    return Double.parseDouble(numStr);
                } catch (NumberFormatException e) {
                    return 0.5;
                }
            }
        }
        return 0.5;
    }

    private String[] extractKeyFactors(LeadScoringContext context) {
        List<String> factors = new ArrayList<>();
        if (context.getCompanySize() != null) {
            factors.add("Company size: " + context.getCompanySize());
        }
        if (context.getIndustry() != null) {
            factors.add("Industry: " + context.getIndustry());
        }
        factors.add("Behavior score: " + context.getBehaviorScore());
        factors.add("Source: " + context.getSource());
        return factors.toArray(new String[0]);
    }

    private String[] extractRiskFactors(String reasoning, DealPredictionContext context) {
        List<String> risks = new ArrayList<>();

        // Check last contact
        if (context.getDaysSinceLastContact() > 7) {
            risks.add("No contact for " + context.getDaysSinceLastContact() + " days");
        }

        // Check days in stage
        if (context.getDaysInStage() > 30) {
            risks.add("Stuck in " + context.getStage() + " for " + context.getDaysInStage() + " days");
        }

        // Check competition
        if (context.getCompetitors() != null && !context.getCompetitors().isEmpty()) {
            risks.add("Competitor involvement: " + context.getCompetitors());
        }

        return risks.toArray(new String[0]);
    }

    private String determineRecommendedAction(Double score, LeadScoringContext context) {
        if (score >= 80) {
            return "Immediate outreach - high priority";
        } else if (score >= 60) {
            return "Schedule demo within 48 hours";
        } else if (score >= 40) {
            return "Nurture with educational content";
        } else {
            return "Add to long-term nurture sequence";
        }
    }

    private String suggestNextAction(DealPredictionContext context, String[] riskFactors) {
        if (riskFactors.length > 2) {
            return "Schedule urgent call to address concerns";
        } else if (!context.isProposalSent()) {
            return "Send proposal immediately";
        } else {
            return "Follow up on proposal decision";
        }
    }

    private LocalDate estimateCloseDate(DealPredictionContext context) {
        int avgDays = context.getAverageDealCycle();
        return LocalDate.now().plusDays((long) avgDays * context.getDaysInStage() / 30);
    }

    private String extractInsightTitle(String response) {
        if (response.contains("\"title\":")) {
            String[] parts = response.split("\"title\":");
            if (parts.length > 1) {
                String title = parts[1].split(",")[0].replace("\"", "").trim();
                return title.length() > 100 ? title.substring(0, 100) : title;
            }
        }
        return "Sales Insight";
    }

    private String[] extractRecommendations(String response) {
        return new String[]{"Review pipeline", "Focus on high-value deals", "Improve follow-up process"};
    }

    // Context classes
    @Data
    @Builder
    public static class LeadScoringContext {
        private String leadName;
        private String companyName;
        private String industry;
        private String companySize;
        private String source;
        private String jobTitle;
        private String emailDomain;
        private Integer behaviorScore;
        private Long assignedRepId;
    }

    @Data
    @Builder
    public static class DealPredictionContext {
        private String dealName;
        private Double dealValue;
        private String stage;
        private Integer daysInStage;
        private String lastContactDate;
        private String competitors;
        private Integer decisionMakers;
        private boolean proposalSent;
        private Integer daysSinceLastContact;
        private Double historicalWinRate;
        private Integer averageDealCycle;
    }

    @Data
    @Builder
    public static class EmailGenerationContext {
        private String recipientName;
        private String recipientCompany;
        private String recipientJobTitle;
        private String purpose;
        private String tone;
        private String[] keyPoints;
        private String context;
    }

    @Data
    @Builder
    public static class AnalyticsInsightContext {
        private String insightType;
        private String period;
        private String summary;
        private Double totalRevenue;
        private Integer dealsWon;
        private Integer dealsLost;
        private Double averageDealSize;
        private Double winRate;
        private Double pipelineValue;
        private Object data;
    }
}