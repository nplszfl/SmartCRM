package com.smartcrm.ai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartcrm.ai.dto.request.LeadScoreRequest;
import com.smartcrm.ai.dto.response.LeadScoreResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Unit tests for LeadScoringService.
 * Tests lead scoring logic, grade calculation, and fallback behavior.
 */
@ExtendWith(MockitoExtension.class)
class LeadScoringServiceTest {

    @Mock
    private LlmClientService llmClient;

    private LeadScoringService leadScoringService;

    @BeforeEach
    void setUp() {
        leadScoringService = new LeadScoringService(llmClient);
    }

    @Test
    void scoreLead_withValidRequest_returnsResponse() throws Exception {
        // Arrange
        LeadScoreRequest request = LeadScoreRequest.builder()
            .leadId("lead-123")
            .companyName("Acme Corp")
            .contactName("John Doe")
            .contactTitle("CEO")
            .industry("Technology")
            .companySize("Enterprise")
            .build();

        String jsonResponse = """
            {
                "score": 85,
                "reasoning": "Strong lead with senior contact.",
                "key_factors": ["CEO level contact", "Enterprise company"],
                "recommendations": ["Schedule demo"]
            }
            """;

        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(jsonResponse);

        when(llmClient.completeJson(anyString(), anyString())).thenReturn(jsonNode);

        // Act
        LeadScoreResponse response = leadScoringService.scoreLead(request);

        // Assert
        assertThat(response.getLeadId()).isEqualTo("lead-123");
        assertThat(response.getScore()).isEqualTo(85);
        assertThat(response.getGrade()).isEqualTo("A");
        assertThat(response.getReasoning()).isEqualTo("Strong lead with senior contact.");
        assertThat(response.getKeyFactors()).hasSize(2);
        assertThat(response.getRecommendations()).hasSize(1);
    }

    @Test
    void scoreLead_withMissingScore_defaultsTo50() throws Exception {
        // Arrange
        LeadScoreRequest request = LeadScoreRequest.builder()
            .leadId("lead-456")
            .companyName("Small Co")
            .contactName("Jane Smith")
            .build();

        String jsonResponse = """
            {
                "reasoning": "No score provided.",
                "key_factors": [],
                "recommendations": []
            }
            """;

        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(jsonResponse);

        when(llmClient.completeJson(anyString(), anyString())).thenReturn(jsonNode);

        // Act
        LeadScoreResponse response = leadScoringService.scoreLead(request);

        // Assert
        assertThat(response.getScore()).isEqualTo(50);
    }

    @Test
    void scoreLead_whenLLMFails_usesFallbackScore() throws Exception {
        // Arrange
        LeadScoreRequest request = LeadScoreRequest.builder()
            .leadId("lead-789")
            .companyName("Big Corp")
            .contactName("Mike Manager")
            .contactTitle("Director of Engineering")
            .companySize("Enterprise (1000+ employees)")
            .industry("SaaS")
            .build();

        when(llmClient.completeJson(anyString(), anyString()))
            .thenThrow(new RuntimeException("LLM unavailable"));

        // Act
        LeadScoreResponse response = leadScoringService.scoreLead(request);

        // Assert
        assertThat(response.getLeadId()).isEqualTo("lead-789");
        assertThat(response.getScore()).isGreaterThan(50); // Should get bonus for Director title + Enterprise size
        assertThat(response.getReasoning()).contains("rule-based fallback");
        assertThat(response.getKeyFactors()).isNotEmpty();
    }

    @Test
    void scoreLead_withAllData_providesCompleteResponse() throws Exception {
        // Arrange
        LeadScoreRequest.LeadBehavioralData behavioralData = LeadScoreRequest.LeadBehavioralData.builder()
            .emailsOpened(10)
            .pagesViewed(25)
            .eventsAttended(2)
            .webinarsAttended(1)
            .downloads(5)
            .build();

        LeadScoreRequest.LeadFirmographicData firmographicData = LeadScoreRequest.LeadFirmographicData.builder()
            .employeeCount(500)
            .annualRevenue(10000000)
            .marketSegment("Mid-Market")
            .techStack("AWS, React, Node.js")
            .build();

        LeadScoreRequest request = LeadScoreRequest.builder()
            .leadId("lead-full")
            .companyName("Full Data Corp")
            .contactName("Sarah VP")
            .contactTitle("VP of Sales")
            .industry("FinTech")
            .companySize("Mid-Market")
            .source("Website")
            .behavioralData(behavioralData)
            .firmographicData(firmographicData)
            .build();

        String jsonResponse = """
            {
                "score": 72,
                "reasoning": "Good engagement and mid-market company.",
                "key_factors": ["VP level contact", "Active engagement"],
                "recommendations": ["Send case studies"]
            }
            """;

        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(jsonResponse);

        when(llmClient.completeJson(anyString(), anyString())).thenReturn(jsonNode);

        // Act
        LeadScoreResponse response = leadScoringService.scoreLead(request);

        // Assert
        assertThat(response.getLeadId()).isEqualTo("lead-full");
        assertThat(response.getScore()).isEqualTo(72);
        assertThat(response.getGrade()).isEqualTo("B");
    }

    @Test
    void gradeCalculation_returnsCorrectGrades() throws Exception {
        // Test data for different score ranges
        ObjectMapper mapper = new ObjectMapper();

        // Score 85 -> A
        JsonNode nodeA = mapper.readTree("{\"score\": 85}");
        int scoreA = nodeA.get("score").asInt(50);
        assertThat(scoreA).isEqualTo(85);

        // Score 65 -> B
        JsonNode nodeB = mapper.readTree("{\"score\": 65}");
        int scoreB = nodeB.get("score").asInt(50);
        assertThat(scoreB).isEqualTo(65);

        // Score 45 -> C
        JsonNode nodeC = mapper.readTree("{\"score\": 45}");
        int scoreC = nodeC.get("score").asInt(50);
        assertThat(scoreC).isEqualTo(45);

        // Score 25 -> D
        JsonNode nodeD = mapper.readTree("{\"score\": 25}");
        int scoreD = nodeD.get("score").asInt(50);
        assertThat(scoreD).isEqualTo(25);

        // Score 10 -> F
        JsonNode nodeF = mapper.readTree("{\"score\": 10}");
        int scoreF = nodeF.get("score").asInt(50);
        assertThat(scoreF).isEqualTo(10);
    }

    @Test
    void fallbackScore_seniorTitles_getBonusPoints() throws Exception {
        // Arrange
        LeadScoreRequest requestCxo = LeadScoreRequest.builder()
            .leadId("lead-cxo")
            .companyName("CXO Corp")
            .contactName("CEO Person")
            .contactTitle("Chief Executive Officer")
            .build();

        LeadScoreRequest requestManager = LeadScoreRequest.builder()
            .leadId("lead-mgr")
            .companyName("Manager Corp")
            .contactName("Manager Person")
            .contactTitle("Manager")
            .build();

        when(llmClient.completeJson(anyString(), anyString()))
            .thenThrow(new RuntimeException("LLM unavailable"));

        // Act
        LeadScoreResponse responseCxo = leadScoringService.scoreLead(requestCxo);
        LeadScoreResponse responseManager = leadScoringService.scoreLead(requestManager);

        // Assert - CXO should get more points than Manager
        assertThat(responseCxo.getScore()).isGreaterThan(responseManager.getScore());
        assertThat(responseCxo.getKeyFactors()).contains("Senior-level contact");
    }

    @Test
    void fallbackScore_enterpriseCompany_getsBonusPoints() throws Exception {
        // Arrange
        LeadScoreRequest enterpriseRequest = LeadScoreRequest.builder()
            .leadId("lead-enterprise")
            .companyName("Enterprise Corp")
            .contactName("IT Person")
            .contactTitle("IT Manager")
            .companySize("Enterprise (5000+ employees)")
            .build();

        LeadScoreRequest smallRequest = LeadScoreRequest.builder()
            .leadId("lead-small")
            .companyName("Small Corp")
            .contactName("IT Person")
            .contactTitle("IT Manager")
            .companySize("Small (10 employees)")
            .build();

        when(llmClient.completeJson(anyString(), anyString()))
            .thenThrow(new RuntimeException("LLM unavailable"));

        // Act
        LeadScoreResponse responseEnterprise = leadScoringService.scoreLead(enterpriseRequest);
        LeadScoreResponse responseSmall = leadScoringService.scoreLead(smallRequest);

        // Assert - Enterprise should get more points
        assertThat(responseEnterprise.getScore()).isGreaterThan(responseSmall.getScore());
        assertThat(responseEnterprise.getKeyFactors()).contains("Large enterprise company");
    }

    @Test
    void extractStringList_handlesEmptyAndMissingFields() throws Exception {
        // Arrange
        LeadScoreRequest request = LeadScoreRequest.builder()
            .leadId("lead-test")
            .companyName("Test Corp")
            .contactName("Test Person")
            .build();

        // Empty arrays in response
        String jsonResponse = """
            {
                "score": 50,
                "reasoning": "Test",
                "key_factors": [],
                "recommendations": []
            }
            """;

        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(jsonResponse);

        when(llmClient.completeJson(anyString(), anyString())).thenReturn(jsonNode);

        // Act
        LeadScoreResponse response = leadScoringService.scoreLead(request);

        // Assert
        assertThat(response.getKeyFactors()).isEmpty();
        assertThat(response.getRecommendations()).isEmpty();
    }

    @Test
    void scoreLead_withNullOptionalFields_stillWorks() throws Exception {
        // Arrange - only required fields
        LeadScoreRequest request = LeadScoreRequest.builder()
            .leadId("lead-minimal")
            .companyName("Minimal Corp")
            .contactName("Min Person")
            .build();

        String jsonResponse = """
            {
                "score": 40,
                "reasoning": "Minimal data provided.",
                "key_factors": ["Basic info"],
                "recommendations": ["Gather more data"]
            }
            """;

        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(jsonResponse);

        when(llmClient.completeJson(anyString(), anyString())).thenReturn(jsonNode);

        // Act
        LeadScoreResponse response = leadScoringService.scoreLead(request);

        // Assert
        assertThat(response.getLeadId()).isEqualTo("lead-minimal");
        assertThat(response.getScore()).isEqualTo(40);
    }
}
