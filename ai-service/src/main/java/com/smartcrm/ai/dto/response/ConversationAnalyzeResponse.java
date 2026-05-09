package com.smartcrm.ai.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationAnalyzeResponse {
    private String conversationId;
    private String overallSentiment;
    private List<String> keyTalkingPoints;
    private List<String> buyingSignals;
    private List<String> objectionRaised;
    private List<String> coachingInsights;
    private List<String> nextRecommendedActions;
}