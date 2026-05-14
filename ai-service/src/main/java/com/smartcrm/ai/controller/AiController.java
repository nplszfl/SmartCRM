package com.smartcrm.ai.controller;

import com.smartcrm.ai.dto.request.ConversationAnalyzeRequest;
import com.smartcrm.ai.dto.request.DealPredictRequest;
import com.smartcrm.ai.dto.request.EmailGenerateRequest;
import com.smartcrm.ai.dto.request.LeadScoreRequest;
import com.smartcrm.ai.dto.request.RepMatchRequest;
import com.smartcrm.ai.dto.response.ConversationAnalyzeResponse;
import com.smartcrm.ai.dto.response.DealPredictResponse;
import com.smartcrm.ai.dto.response.EmailGenerateResponse;
import com.smartcrm.ai.dto.response.HealthResponse;
import com.smartcrm.ai.dto.response.LeadScoreResponse;
import com.smartcrm.ai.dto.response.RepMatchResponse;
import com.smartcrm.ai.service.ConversationAnalysisService;
import com.smartcrm.ai.service.DealPredictionService;
import com.smartcrm.ai.service.EmailGenerationService;
import com.smartcrm.ai.service.LeadScoringService;
import com.smartcrm.ai.service.RepMatchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * AI Service REST Controller
 * Provides AI-powered CRM features: lead scoring, deal prediction, email generation,
 * conversation analysis, and intelligent rep matching
 */
@Slf4j
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final LeadScoringService leadScoringService;
    private final DealPredictionService dealPredictionService;
    private final EmailGenerationService emailGenerationService;
    private final ConversationAnalysisService conversationAnalysisService;
    private final RepMatchService repMatchService;

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<HealthResponse> health() {
        return ResponseEntity.ok(HealthResponse.builder()
                .status("UP")
                .service("SmartCRM AI Service")
                .version("1.0.0")
                .build());
    }

    /**
     * Score a lead using AI analysis
     */
    @PostMapping("/leads/score")
    public ResponseEntity<LeadScoreResponse> scoreLead(@Valid @RequestBody LeadScoreRequest request) {
        log.info("Received lead scoring request for lead: {}", request.getLeadId());
        LeadScoreResponse response = leadScoringService.scoreLead(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Predict deal outcomes
     */
    @PostMapping("/deals/predict")
    public ResponseEntity<DealPredictResponse> predictDeal(@Valid @RequestBody DealPredictRequest request) {
        log.info("Received deal prediction request for deal: {}", request.getDealId());
        DealPredictResponse response = dealPredictionService.predictDeal(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Generate personalized email content
     */
    @PostMapping("/emails/generate")
    public ResponseEntity<EmailGenerateResponse> generateEmail(@Valid @RequestBody EmailGenerateRequest request) {
        log.info("Received email generation request");
        EmailGenerateResponse response = emailGenerationService.generateEmail(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Analyze sales conversation transcript
     */
    @PostMapping("/conversations/analyze")
    public ResponseEntity<ConversationAnalyzeResponse> analyzeConversation(
            @Valid @RequestBody ConversationAnalyzeRequest request) {
        log.info("Received conversation analysis request for: {}", request.getConversationId());
        ConversationAnalyzeResponse response = conversationAnalysisService.analyzeConversation(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Match lead to best sales rep
     */
    @PostMapping("/leads/match-rep")
    public ResponseEntity<RepMatchResponse> matchRep(@Valid @RequestBody RepMatchRequest request) {
        log.info("Received rep match request for lead: {}", request.getLeadId());
        RepMatchResponse response = repMatchService.matchRep(request);
        return ResponseEntity.ok(response);
    }
}
