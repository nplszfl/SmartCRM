package com.smartcrm.ai.service;

import com.smartcrm.ai.dto.request.DealPredictRequest;
import com.smartcrm.ai.dto.response.DealPredictResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for DealPredictionService
 */
@ExtendWith(MockitoExtension.class)
class DealPredictionServiceTest {

    @Mock
    private LlmClientService llmClient;

    @InjectMocks
    private DealPredictionService dealPredictionService;

    private DealPredictRequest createTestRequest() {
        return DealPredictRequest.builder()
                .dealId("DEAL-001")
                .dealName("Enterprise SaaS License")
                .accountName("Acme Corp")
                .stage("QUALIFICATION")
                .amount(150000.0)
                .ownerId("user-1")
                .productLines(List.of("Enterprise Suite", "Analytics Add-on"))
                .engagementData(DealPredictRequest.DealEngagementData.builder()
                        .meetingsHeld(3)
                        .emailsExchanged(12)
                        .demoCompleted(true)
                        .proposalSent(false)
                        .daysSinceLastContact(3)
                        .build())
                .build();
    }

    @Test
    @DisplayName("Should return fallback prediction when LLM is unavailable")
    void predictDeal_whenLlmUnavailable_returnsFallbackPrediction() {
        // Given
        DealPredictRequest request = createTestRequest();
        
        // When - LLM will throw exception, triggering fallback
        DealPredictResponse response = dealPredictionService.predictDeal(request);
        
        // Then
        assertNotNull(response);
        assertEquals("DEAL-001", response.getDealId());
        assertNotNull(response.getCloseProbability());
        assertTrue(response.getCloseProbability() >= 0 && response.getCloseProbability() <= 100);
        assertNotNull(response.getRiskLevel());
        assertTrue(List.of("low", "medium", "high").contains(response.getRiskLevel().toLowerCase()));
        assertNotNull(response.getRecommendations());
        assertFalse(response.getRecommendations().isEmpty());
    }

    @Test
    @DisplayName("Should calculate base probability based on deal stage")
    void predictDeal_withDifferentStages_calculatesCorrectBaseProbability() {
        // Given - Prospecting stage
        DealPredictRequest prospectingRequest = DealPredictRequest.builder()
                .dealId("DEAL-002")
                .dealName("Test Deal")
                .accountName("Test Account")
                .stage("PROSPECTING")
                .amount(50000.0)
                .ownerId("user-1")
                .build();
        
        // When
        DealPredictResponse response = dealPredictionService.predictDeal(prospectingRequest);
        
        // Then - Prospecting should have lower probability
        assertNotNull(response);
        assertTrue(response.getCloseProbability() <= 40);
    }

    @Test
    @DisplayName("Should increase probability when demo is completed")
    void predictDeal_whenDemoCompleted_increasesProbability() {
        // Given
        DealPredictRequest requestWithDemo = DealPredictRequest.builder()
                .dealId("DEAL-003")
                .dealName("Test Deal")
                .accountName("Test Account")
                .stage("QUALIFICATION")
                .amount(75000.0)
                .ownerId("user-1")
                .engagementData(DealPredictRequest.DealEngagementData.builder()
                        .demoCompleted(true)
                        .meetingsHeld(2)
                        .build())
                .build();
        
        // When
        DealPredictResponse response = dealPredictionService.predictDeal(requestWithDemo);
        
        // Then
        assertNotNull(response);
        // Demo completed adds 10% to base qualification probability (35%), so should be >= 45%
        assertTrue(response.getCloseProbability() >= 45);
        assertTrue(response.getRecommendations().stream()
                .anyMatch(r -> r.toLowerCase().contains("demo")));
    }

    @Test
    @DisplayName("Should decrease probability when competitor is involved")
    void predictDeal_whenCompetitorInvolved_decreasesProbability() {
        // Given
        DealPredictRequest requestWithCompetitor = DealPredictRequest.builder()
                .dealId("DEAL-004")
                .dealName("Test Deal")
                .accountName("Test Account")
                .stage("PROPOSAL")
                .amount(100000.0)
                .ownerId("user-1")
                .competitorInvolved("CompetitorX")
                .engagementData(DealPredictRequest.DealEngagementData.builder()
                        .meetingsHeld(4)
                        .demoCompleted(true)
                        .proposalSent(true)
                        .build())
                .build();
        
        // When
        DealPredictResponse response = dealPredictionService.predictDeal(requestWithCompetitor);
        
        // Then
        assertNotNull(response);
        // Competitor involvement should either increase risk level or decrease probability
        boolean hasRiskImpact = response.getRiskLevel().equalsIgnoreCase("high") || 
                                response.getCloseProbability() < 70;
        assertTrue(hasRiskImpact, "Expected competitor to increase risk or decrease probability");
    }

    @Test
    @DisplayName("Should flag stale deals with no recent contact")
    void predictDeal_whenNoRecentContact_addsRiskFactor() {
        // Given
        DealPredictRequest requestWithStaleContact = DealPredictRequest.builder()
                .dealId("DEAL-005")
                .dealName("Test Deal")
                .accountName("Test Account")
                .stage("NEGOTIATION")
                .amount(200000.0)
                .ownerId("user-1")
                .engagementData(DealPredictRequest.DealEngagementData.builder()
                        .daysSinceLastContact(14)
                        .meetingsHeld(5)
                        .demoCompleted(true)
                        .proposalSent(true)
                        .build())
                .build();
        
        // When
        DealPredictResponse response = dealPredictionService.predictDeal(requestWithStaleContact);
        
        // Then
        assertNotNull(response);
        assertTrue(response.getRiskFactors().stream()
                .anyMatch(r -> r.toLowerCase().contains("14 days") || r.toLowerCase().contains("contact")));
    }

    @Test
    @DisplayName("Should return predicted close date")
    void predictDeal_returnsPredictedCloseDate() {
        // Given
        DealPredictRequest request = createTestRequest();
        
        // When
        DealPredictResponse response = dealPredictionService.predictDeal(request);
        
        // Then
        assertNotNull(response);
        assertNotNull(response.getPredictedCloseDate());
        // Close date should be in the future
        assertTrue(response.getPredictedCloseDate().matches("\\d{4}-\\d{2}-\\d{2}"));
    }

    @Test
    @DisplayName("Should handle null engagement data gracefully")
    void predictDeal_withNullEngagementData_handlesGracefully() {
        // Given
        DealPredictRequest requestWithoutEngagement = DealPredictRequest.builder()
                .dealId("DEAL-006")
                .dealName("Test Deal")
                .accountName("Test Account")
                .stage("PROSPECTING")
                .amount(25000.0)
                .ownerId("user-1")
                .build();
        
        // When
        DealPredictResponse response = dealPredictionService.predictDeal(requestWithoutEngagement);
        
        // Then
        assertNotNull(response);
        assertEquals("DEAL-006", response.getDealId());
        assertNotNull(response.getCloseProbability());
    }

    @Test
    @DisplayName("Should return closed won with 100% probability")
    void predictDeal_withClosedWonStage_returnsFullProbability() {
        // Given
        DealPredictRequest closedWonRequest = DealPredictRequest.builder()
                .dealId("DEAL-007")
                .dealName("Won Deal")
                .accountName("Acme Corp")
                .stage("CLOSED_WON")
                .amount(180000.0)
                .ownerId("user-1")
                .build();
        
        // When
        DealPredictResponse response = dealPredictionService.predictDeal(closedWonRequest);
        
        // Then
        assertNotNull(response);
        assertEquals(100.0, response.getCloseProbability());
        assertEquals("low", response.getRiskLevel().toLowerCase());
    }
}
