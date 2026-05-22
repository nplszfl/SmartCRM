package com.smartcrm.crm.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartcrm.common.exception.ResourceNotFoundException;
import com.smartcrm.crm.dto.CampaignRequest;
import com.smartcrm.crm.entity.Campaign;
import com.smartcrm.crm.repository.CampaignRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CampaignService.
 * Tests campaign lifecycle, status transitions, and ROI calculations.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CampaignServiceTest {

    @Mock
    private CampaignRepository campaignRepository;

    private CampaignService campaignService;

    @BeforeEach
    void setUp() {
        campaignService = new CampaignService();
        try {
            var field = ServiceImpl.class.getDeclaredField("baseMapper");
            field.setAccessible(true);
            field.set(campaignService, campaignRepository);
        } catch (Exception e) {
            // Fallback - the service uses this.baseMapper internally
        }
    }

    @Test
    void createCampaign_withValidRequest_setsDefaultValues() {
        // Arrange
        CampaignRequest request = new CampaignRequest();
        request.setName("Summer Sale Campaign");
        request.setType("EMAIL");
        request.setBudget(new BigDecimal("5000"));
        request.setOwnerId(1L);
        request.setOwnerName("John Doe");

        when(campaignRepository.insert(any(Campaign.class))).thenReturn(1);

        // Act
        Campaign result = campaignService.createCampaign(request);

        // Assert
        assertThat(result.getStatus()).isEqualTo("PLANNING");
        assertThat(result.getType()).isEqualTo("EMAIL");
        assertThat(result.getCurrency()).isEqualTo("USD");
        assertThat(result.getSpent()).isEqualByComparingTo(BigDecimal.ZERO);
        verify(campaignRepository).insert(any(Campaign.class));
    }

    @Test
    void createCampaign_withNullType_defaultsToEmail() {
        // Arrange
        CampaignRequest request = new CampaignRequest();
        request.setName("Test Campaign");
        request.setType(null);

        when(campaignRepository.insert(any(Campaign.class))).thenReturn(1);

        // Act
        Campaign result = campaignService.createCampaign(request);

        // Assert
        assertThat(result.getType()).isEqualTo("EMAIL");
    }

    @Test
    void getCampaignById_whenExists_returnsCampaign() {
        // Arrange
        Long campaignId = 1L;
        Campaign campaign = new Campaign();
        campaign.setId(campaignId);
        campaign.setName("Test Campaign");
        campaign.setStatus("ACTIVE");
        campaign.setBudget(new BigDecimal("10000"));

        when(campaignRepository.selectById(campaignId)).thenReturn(campaign);

        // Act
        Campaign result = campaignService.getCampaignById(campaignId);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Test Campaign");
    }

    @Test
    void getCampaignById_whenNotExists_throwsResourceNotFoundException() {
        // Arrange
        Long campaignId = 999L;
        when(campaignRepository.selectById(campaignId)).thenReturn(null);

        // Act & Assert
        assertThatThrownBy(() -> campaignService.getCampaignById(campaignId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Campaign");
    }

    @Test
    void activateCampaign_setsStatusToActive() {
        // Arrange
        Long campaignId = 1L;
        Campaign existing = new Campaign();
        existing.setId(campaignId);
        existing.setName("Test");
        existing.setStatus("PLANNING");

        when(campaignRepository.selectById(campaignId)).thenReturn(existing);
        when(campaignRepository.updateById(any(Campaign.class))).thenReturn(1);

        // Act
        Campaign result = campaignService.activateCampaign(campaignId);

        // Assert
        assertThat(result.getStatus()).isEqualTo("ACTIVE");
        assertThat(result.getStartDate()).isNotNull();
    }

    @Test
    void pauseCampaign_setsStatusToPaused() {
        // Arrange
        Long campaignId = 1L;
        Campaign existing = new Campaign();
        existing.setId(campaignId);
        existing.setName("Test");
        existing.setStatus("ACTIVE");

        when(campaignRepository.selectById(campaignId)).thenReturn(existing);
        when(campaignRepository.updateById(any(Campaign.class))).thenReturn(1);

        // Act
        Campaign result = campaignService.pauseCampaign(campaignId);

        // Assert
        assertThat(result.getStatus()).isEqualTo("PAUSED");
    }

    @Test
    void completeCampaign_setsStatusToCompletedAndEndDate() {
        // Arrange
        Long campaignId = 1L;
        Campaign existing = new Campaign();
        existing.setId(campaignId);
        existing.setName("Test");
        existing.setStatus("ACTIVE");

        when(campaignRepository.selectById(campaignId)).thenReturn(existing);
        when(campaignRepository.updateById(any(Campaign.class))).thenReturn(1);

        // Act
        Campaign result = campaignService.completeCampaign(campaignId);

        // Assert
        assertThat(result.getStatus()).isEqualTo("COMPLETED");
        assertThat(result.getEndDate()).isNotNull();
    }

    @Test
    void updateMetrics_updatesAllMetrics() {
        // Arrange
        Long campaignId = 1L;
        Campaign existing = new Campaign();
        existing.setId(campaignId);
        existing.setName("Test");
        existing.setImpressions(0);
        existing.setClicks(0);

        when(campaignRepository.selectById(campaignId)).thenReturn(existing);
        when(campaignRepository.updateById(any(Campaign.class))).thenReturn(1);

        // Act
        Campaign result = campaignService.updateMetrics(
                campaignId, 10000, 500, 50, 10, 
                new BigDecimal("5000"), new BigDecimal("2000"));

        // Assert
        assertThat(result.getImpressions()).isEqualTo(10000);
        assertThat(result.getClicks()).isEqualTo(500);
        assertThat(result.getLeadsGenerated()).isEqualTo(50);
        assertThat(result.getConversions()).isEqualTo(10);
        assertThat(result.getRevenue()).isEqualByComparingTo(new BigDecimal("5000"));
        assertThat(result.getSpent()).isEqualByComparingTo(new BigDecimal("2000"));
    }

    @Test
    void calculateCampaignRoi_calculatesCorrectly() {
        // Arrange
        Long campaignId = 1L;
        Campaign existing = new Campaign();
        existing.setId(campaignId);
        existing.setName("Test");
        existing.setRevenue(new BigDecimal("15000"));
        existing.setSpent(new BigDecimal("5000"));

        when(campaignRepository.selectById(campaignId)).thenReturn(existing);

        // Act
        BigDecimal roi = campaignService.calculateCampaignRoi(campaignId);

        // Assert
        // ROI = (15000 - 5000) / 5000 * 100 = 200%
        assertThat(roi).isEqualByComparingTo(new BigDecimal("200.0000"));
    }

    @Test
    void calculateCampaignRoi_withZeroSpent_returnsZero() {
        // Arrange
        Long campaignId = 1L;
        Campaign existing = new Campaign();
        existing.setId(campaignId);
        existing.setName("Test");
        existing.setRevenue(new BigDecimal("15000"));
        existing.setSpent(BigDecimal.ZERO);

        when(campaignRepository.selectById(campaignId)).thenReturn(existing);

        // Act
        BigDecimal roi = campaignService.calculateCampaignRoi(campaignId);

        // Assert
        assertThat(roi).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void trackLead_incrementsLeadCount() {
        // Arrange
        Long campaignId = 1L;
        Campaign existing = new Campaign();
        existing.setId(campaignId);
        existing.setName("Test");
        existing.setLeadsGenerated(10);

        when(campaignRepository.selectById(campaignId)).thenReturn(existing);
        when(campaignRepository.updateById(any(Campaign.class))).thenReturn(1);

        // Act
        Campaign result = campaignService.trackLead(campaignId);

        // Assert
        assertThat(result.getLeadsGenerated()).isEqualTo(11);
    }

    @Test
    void trackConversion_incrementsConversionCount() {
        // Arrange
        Long campaignId = 1L;
        Campaign existing = new Campaign();
        existing.setId(campaignId);
        existing.setName("Test");
        existing.setConversions(5);

        when(campaignRepository.selectById(campaignId)).thenReturn(existing);
        when(campaignRepository.updateById(any(Campaign.class))).thenReturn(1);

        // Act
        Campaign result = campaignService.trackConversion(campaignId);

        // Assert
        assertThat(result.getConversions()).isEqualTo(6);
    }

    @Test
    void getActiveCampaigns_returnsOnlyActiveCampaigns() {
        // Arrange
        Campaign active1 = new Campaign();
        active1.setId(1L);
        active1.setName("Active 1");
        active1.setStatus("ACTIVE");
        
        Campaign active2 = new Campaign();
        active2.setId(2L);
        active2.setName("Active 2");
        active2.setStatus("ACTIVE");

        when(campaignRepository.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(List.of(active1, active2));

        // Act
        List<Campaign> result = campaignService.getActiveCampaigns();

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result).allMatch(c -> "ACTIVE".equals(c.getStatus()));
    }

    @Test
    void getCampaignsByOwner_returnsOwnerCampaigns() {
        // Arrange
        Long ownerId = 100L;
        Campaign campaign = new Campaign();
        campaign.setId(1L);
        campaign.setName("Owner Campaign");
        campaign.setOwnerId(ownerId);

        when(campaignRepository.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(List.of(campaign));

        // Act
        List<Campaign> result = campaignService.getCampaignsByOwner(ownerId);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getOwnerId()).isEqualTo(ownerId);
    }

    @Test
    void addAiRecommendation_updatesAiFields() {
        // Arrange
        Long campaignId = 1L;
        Campaign existing = new Campaign();
        existing.setId(campaignId);
        existing.setName("Test");

        when(campaignRepository.selectById(campaignId)).thenReturn(existing);
        when(campaignRepository.updateById(any(Campaign.class))).thenReturn(1);

        // Act
        Campaign result = campaignService.addAiRecommendation(
                campaignId, "Increase budget for high-performing channels", 15.5);

        // Assert
        assertThat(result.getAiRecommendation()).isEqualTo("Increase budget for high-performing channels");
        assertThat(result.getPredictedRoi()).isEqualTo(15.5);
    }

    @Test
    void countByStatus_returnsCorrectCount() {
        // Arrange
        String status = "ACTIVE";
        when(campaignRepository.selectCount(any(LambdaQueryWrapper.class))).thenReturn(5L);

        // Act
        long count = campaignService.countByStatus(status);

        // Assert
        assertThat(count).isEqualTo(5L);
    }
}
