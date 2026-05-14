package com.smartcrm.ai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.smartcrm.ai.dto.request.ConversationAnalyzeRequest;
import com.smartcrm.ai.dto.response.ConversationAnalyzeResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Conversation Analysis Service
 * Analyzes sales conversation transcripts to extract insights, sentiment, and coaching recommendations
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConversationAnalysisService {

    private final LlmClientService llmClient;

    /**
     * Analyze a sales conversation transcript
     */
    public ConversationAnalyzeResponse analyzeConversation(ConversationAnalyzeRequest request) {
        log.info("Analyzing conversation: {}", request.getConversationId());

        try {
            String prompt = buildAnalysisPrompt(request);
            String systemPrompt = llmClient.getSystemPromptForType("conversation_analysis");

            JsonNode result = llmClient.completeJson(prompt, systemPrompt);

            String overallSentiment = extractString(result, "sentiment", "neutral");
            List<String> keyTalkingPoints = extractStringList(result, "key_points");
            List<String> buyingSignals = extractStringList(result, "buying_signals");
            List<String> objectionRaised = extractStringList(result, "objections");
            List<String> coachingInsights = extractStringList(result, "coaching_insights");
            List<String> nextRecommendedActions = extractStringList(result, "next_actions");

            return ConversationAnalyzeResponse.builder()
                    .conversationId(request.getConversationId())
                    .overallSentiment(overallSentiment)
                    .keyTalkingPoints(keyTalkingPoints)
                    .buyingSignals(buyingSignals)
                    .objectionRaised(objectionRaised)
                    .coachingInsights(coachingInsights)
                    .nextRecommendedActions(nextRecommendedActions)
                    .build();

        } catch (Exception e) {
            log.error("Conversation analysis error for {}: {}", request.getConversationId(), e.getMessage());
            return fallbackAnalysis(request);
        }
    }

    private String buildAnalysisPrompt(ConversationAnalyzeRequest request) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Analyze this sales conversation transcript and provide detailed insights.\n\n");
        prompt.append("CONVERSATION METADATA:\n");
        prompt.append("- Conversation ID: ").append(request.getConversationId()).append("\n");
        prompt.append("- Speaker: ").append(request.getSpeakerName()).append("\n");
        prompt.append("- Role: ").append(request.getSpeakerRole()).append("\n");
        prompt.append("- Type: ").append(request.getConversationType()).append("\n");
        prompt.append("\nTRANSCRIPT:\n");
        prompt.append(request.getTranscript());
        prompt.append("""
                
                Provide your analysis in JSON format:
                {
                    "sentiment": "<positive/neutral/negative>",
                    "key_points": ["<key point 1>", "<key point 2>", "<key point 3>"],
                    "buying_signals": ["<signal 1>", "<signal 2>"],
                    "objections": ["<objection 1>", "<objection 2>"],
                    "coaching_insights": ["<insight 1>", "<insight 2>", "<insight 3>"],
                    "next_actions": ["<action 1>", "<action 2>"]
                }
                
                Focus on: communication effectiveness, buyer engagement, objection handling, and next steps.
                """);

        return prompt.toString();
    }

    private ConversationAnalyzeResponse fallbackAnalysis(ConversationAnalyzeRequest request) {
        log.warn("Using rule-based fallback analysis for conversation: {}", request.getConversationId());

        // Simple rule-based analysis as fallback
        String transcript = request.getTranscript().toLowerCase();
        
        // Detect sentiment keywords
        int positiveCount = 0;
        int negativeCount = 0;
        
        String[] positiveWords = {"great", "excellent", "perfect", "love", "interested", "good", "like", "want", "need", "buy"};
        String[] negativeWords = {"concern", "problem", "issue", "difficult", "bad", "hate", "dislike", "dont", "cant", "wont", "budget", "price"};
        
        for (String word : positiveWords) {
            if (transcript.contains(word)) positiveCount++;
        }
        for (String word : negativeWords) {
            if (transcript.contains(word)) negativeCount++;
        }
        
        String overallSentiment;
        if (positiveCount > negativeCount) {
            overallSentiment = "positive";
        } else if (negativeCount > positiveCount) {
            overallSentiment = "negative";
        } else {
            overallSentiment = "neutral";
        }
        
        // Detect basic buying signals
        List<String> buyingSignals = new ArrayList<>();
        if (transcript.contains("when can we") || transcript.contains("let's proceed")) {
            buyingSignals.add("Expressing intent to move forward");
        }
        if (transcript.contains("pricing") || transcript.contains("quote")) {
            buyingSignals.add("Asking about pricing - evaluating budget");
        }
        if (transcript.contains("timeline") || transcript.contains("schedule")) {
            buyingSignals.add("Considering implementation timeline");
        }
        
        // Detect objections
        List<String> objectionRaised = new ArrayList<>();
        if (transcript.contains("budget") || transcript.contains("cost") || transcript.contains("expensive")) {
            objectionRaised.add("Price/budget concern");
        }
        if (transcript.contains("competing") || transcript.contains("alternative") || transcript.contains("other")) {
            objectionRaised.add("Evaluating alternatives");
        }
        if (transcript.contains("need to") || transcript.contains("thinking")) {
            objectionRaised.add("Not ready to decide");
        }
        
        // Basic coaching insights
        List<String> coachingInsights = new ArrayList<>();
        if (negativeCount > positiveCount) {
            coachingInsights.add("Address concerns before pushing for decision");
            coachingInsights.add("Consider providing case studies or references");
        }
        if (positiveCount > 3) {
            coachingInsights.add("Good rapport building - maintain this approach");
        }
        coachingInsights.add("Focus on value proposition if price objection arises");
        
        // Next actions
        List<String> nextRecommendedActions = new ArrayList<>();
        nextRecommendedActions.add("Follow up with relevant case studies");
        nextRecommendedActions.add("Schedule next steps discussion");
        
        return ConversationAnalyzeResponse.builder()
                .conversationId(request.getConversationId())
                .overallSentiment(overallSentiment)
                .keyTalkingPoints(List.of("Conversation analyzed", "Key topics discussed"))
                .buyingSignals(buyingSignals.isEmpty() ? List.of("No strong signals detected") : buyingSignals)
                .objectionRaised(objectionRaised.isEmpty() ? List.of("No major objections raised") : objectionRaised)
                .coachingInsights(coachingInsights)
                .nextRecommendedActions(nextRecommendedActions)
                .build();
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
}
