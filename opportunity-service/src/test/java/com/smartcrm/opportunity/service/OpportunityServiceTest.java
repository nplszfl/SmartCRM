package com.smartcrm.opportunity.service;

import com.smartcrm.common.exception.ResourceNotFoundException;
import com.smartcrm.opportunity.dto.OpportunityRequest;
import com.smartcrm.opportunity.entity.Opportunity;
import com.smartcrm.opportunity.repository.OpportunityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for OpportunityService.
 * Tests opportunity lifecycle, stage transitions, and pipeline calculations.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class OpportunityServiceTest {

    @Mock
    private OpportunityRepository opportunityRepository;

    private OpportunityService opportunityService;

    @BeforeEach
    void setUp() {
        opportunityService = new OpportunityService();
        try {
            var field = ServiceImpl.class.getDeclaredField("baseMapper");
            field.setAccessible(true);
            field.set(opportunityService, opportunityRepository);
        } catch (Exception e) {
            // Fallback - the service uses this.baseMapper internally
        }
    }

    @Test
    void createOpportunity_withValidRequest_setsDefaultStage() {
        // Arrange
        OpportunityRequest request = new OpportunityRequest();
        request.setName("Enterprise Deal");
        request.setAmount(new BigDecimal("50000"));
        request.setStage("PROSPECTING");

        when(opportunityRepository.insert(any(Opportunity.class))).thenReturn(1);

        // Act
        Opportunity result = opportunityService.createOpportunity(request);

        // Assert
        assertThat(result.getStage()).isEqualTo("PROSPECTING");
        assertThat(result.getProbability()).isEqualByComparingTo(new BigDecimal("10"));
        verify(opportunityRepository).insert(any(Opportunity.class));
    }

    @Test
    void createOpportunity_withNullStage_defaultsToProspecting() {
        // Arrange
        OpportunityRequest request = new OpportunityRequest();
        request.setName("New Opportunity");
        request.setAmount(new BigDecimal("25000"));
        request.setStage(null);

        when(opportunityRepository.insert(any(Opportunity.class))).thenReturn(1);

        // Act
        Opportunity result = opportunityService.createOpportunity(request);

        // Assert
        assertThat(result.getStage()).isEqualTo("PROSPECTING");
    }

    @Test
    void createOpportunity_calculatesProbabilityBasedOnStage() {
        // Arrange
        OpportunityRequest request = new OpportunityRequest();
        request.setName("Negotiation Deal");
        request.setAmount(new BigDecimal("100000"));
        request.setStage("NEGOTIATION");

        when(opportunityRepository.insert(any(Opportunity.class))).thenReturn(1);

        // Act
        Opportunity result = opportunityService.createOpportunity(request);

        // Assert
        assertThat(result.getProbability()).isEqualByComparingTo(new BigDecimal("75"));
    }

    @Test
    void getOpportunityById_whenExists_returnsOpportunity() {
        // Arrange
        Long oppId = 1L;
        Opportunity opp = new Opportunity();
        opp.setId(oppId);
        opp.setName("Test Opportunity");
        opp.setAmount(new BigDecimal("30000"));
        opp.setStage("PROPOSAL");

        when(opportunityRepository.selectById(oppId)).thenReturn(opp);

        // Act
        Opportunity result = opportunityService.getOpportunityById(oppId);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Test Opportunity");
    }

    @Test
    void getOpportunityById_whenNotExists_throwsResourceNotFoundException() {
        // Arrange
        Long oppId = 999L;
        when(opportunityRepository.selectById(oppId)).thenReturn(null);

        // Act & Assert
        assertThatThrownBy(() -> opportunityService.getOpportunityById(oppId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Opportunity");
    }

    @Test
    void moveToStage_updatesStageAndProbability() {
        // Arrange
        Long oppId = 1L;
        Opportunity existing = new Opportunity();
        existing.setId(oppId);
        existing.setName("Test");
        existing.setStage("PROSPECTING");
        existing.setProbability(new BigDecimal("10"));

        when(opportunityRepository.selectById(oppId)).thenReturn(existing);
        when(opportunityRepository.updateById(any(Opportunity.class))).thenReturn(1);

        // Act
        Opportunity result = opportunityService.moveToStage(oppId, "PROPOSAL");

        // Assert
        assertThat(result.getStage()).isEqualTo("PROPOSAL");
        assertThat(result.getProbability()).isEqualByComparingTo(new BigDecimal("50"));
    }

    @Test
    void closeAsWon_setsClosedWonStatusAndProbability() {
        // Arrange
        Long oppId = 1L;
        Opportunity existing = new Opportunity();
        existing.setId(oppId);
        existing.setName("Test");
        existing.setStage("NEGOTIATION");
        existing.setProbability(new BigDecimal("75"));

        when(opportunityRepository.selectById(oppId)).thenReturn(existing);
        when(opportunityRepository.updateById(any(Opportunity.class))).thenReturn(1);

        // Act
        Opportunity result = opportunityService.closeAsWon(oppId);

        // Assert
        assertThat(result.getStage()).isEqualTo("CLOSED_WON");
        assertThat(result.getProbability()).isEqualByComparingTo(new BigDecimal("100"));
        assertThat(result.getActualCloseDate()).isNotNull();
    }

    @Test
    void closeAsLost_setsClosedLostStatusAndZeroProbability() {
        // Arrange
        Long oppId = 1L;
        Opportunity existing = new Opportunity();
        existing.setId(oppId);
        existing.setName("Test");
        existing.setStage("PROPOSAL");

        when(opportunityRepository.selectById(oppId)).thenReturn(existing);
        when(opportunityRepository.updateById(any(Opportunity.class))).thenReturn(1);

        // Act
        Opportunity result = opportunityService.closeAsLost(oppId);

        // Assert
        assertThat(result.getStage()).isEqualTo("CLOSED_LOST");
        assertThat(result.getProbability()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void getTotalPipelineValue_sumsOpenOpportunities() {
        // Arrange
        Opportunity opp1 = new Opportunity();
        opp1.setAmount(new BigDecimal("10000"));
        opp1.setStage("PROSPECTING");
        
        Opportunity opp2 = new Opportunity();
        opp2.setAmount(new BigDecimal("20000"));
        opp2.setStage("QUALIFICATION");
        
        Opportunity opp3 = new Opportunity();
        opp3.setAmount(new BigDecimal("30000"));
        opp3.setStage("CLOSED_WON"); // Should not be included

        when(opportunityRepository.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(List.of(opp1, opp2));

        // Act
        BigDecimal result = opportunityService.getTotalPipelineValue();

        // Assert
        assertThat(result).isEqualByComparingTo(new BigDecimal("30000"));
    }

    @Test
    void getWeightedPipelineValue_appliesProbabilityWeights() {
        // Arrange
        Opportunity opp1 = new Opportunity();
        opp1.setAmount(new BigDecimal("10000"));
        opp1.setProbability(new BigDecimal("10")); // 10% = 1000 weighted
        
        Opportunity opp2 = new Opportunity();
        opp2.setAmount(new BigDecimal("10000"));
        opp2.setProbability(new BigDecimal("50")); // 50% = 5000 weighted

        when(opportunityRepository.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(List.of(opp1, opp2));

        // Act
        BigDecimal result = opportunityService.getWeightedPipelineValue();

        // Assert
        // 10000 * 0.10 + 10000 * 0.50 = 1000 + 5000 = 6000
        assertThat(result).isEqualByComparingTo(new BigDecimal("6000"));
    }

    @Test
    void getOpportunitiesByStage_returnsFilteredOpportunities() {
        // Arrange
        String stage = "PROPOSAL";
        Opportunity opp1 = new Opportunity();
        opp1.setId(1L);
        opp1.setName("Deal 1");
        opp1.setStage("PROPOSAL");
        
        Opportunity opp2 = new Opportunity();
        opp2.setId(2L);
        opp2.setName("Deal 2");
        opp2.setStage("PROPOSAL");

        when(opportunityRepository.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(List.of(opp1, opp2));

        // Act
        List<Opportunity> result = opportunityService.getOpportunitiesByStage(stage);

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result).allMatch(o -> "PROPOSAL".equals(o.getStage()));
    }

    @Test
    void getOpportunitiesByOwner_returnsOwnerOpportunities() {
        // Arrange
        Long ownerId = 100L;
        Opportunity opp = new Opportunity();
        opp.setId(1L);
        opp.setName("Owner Deal");
        opp.setOwnerId(ownerId);

        when(opportunityRepository.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(List.of(opp));

        // Act
        List<Opportunity> result = opportunityService.getOpportunitiesByOwner(ownerId);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getOwnerId()).isEqualTo(ownerId);
    }

    @Test
    void updateAiPrediction_updatesAiFields() {
        // Arrange
        Long oppId = 1L;
        Opportunity existing = new Opportunity();
        existing.setId(oppId);
        existing.setName("Test");

        when(opportunityRepository.selectById(oppId)).thenReturn(existing);
        when(opportunityRepository.updateById(any(Opportunity.class))).thenReturn(1);

        // Act
        Opportunity result = opportunityService.updateAiPrediction(oppId, "WILL_CLOSE", 0.85);

        // Assert
        assertThat(result.getAiPrediction()).isEqualTo("WILL_CLOSE");
        assertThat(result.getAiConfidenceScore()).isEqualTo(0.85);
    }

    @Test
    void countByStage_returnsCorrectCount() {
        // Arrange
        String stage = "QUALIFICATION";
        when(opportunityRepository.selectCount(any(LambdaQueryWrapper.class))).thenReturn(15L);

        // Act
        long count = opportunityService.countByStage(stage);

        // Assert
        assertThat(count).isEqualTo(15L);
    }
}
