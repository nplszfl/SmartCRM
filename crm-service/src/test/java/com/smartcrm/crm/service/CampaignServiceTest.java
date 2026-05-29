package com.smartcrm.crm.service;

import com.smartcrm.crm.dto.CampaignRequest;
import com.smartcrm.crm.entity.Campaign;
import com.smartcrm.crm.repository.CampaignRepository;
import com.smartcrm.common.exception.ResourceNotFoundException;
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
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CampaignService.
 * Tests campaign lifecycle operations, metrics tracking, and ROI calculations.
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
    void createCampaign_withValidData_setsDefaultStatusAndMetrics() {
        // Arrange
        CampaignRequest request = new CampaignRequest();
        request.setName("Summer Sale Campaign");
        request.setType("EMAIL");
        request.setBudget(new BigDecimal("5000.00"));
        request.setCurrency("USD");
        request.setOwnerId(1L);
        request.setOwnerName("John Doe");

        when(campaignRepository.insert(any(Campaign.class))).thenReturn(1);

        // Act
        Campaign result = campaignService.createCampaign(request);

        // Assert
        assertThat(result.getName()).isEqualTo("Summer Sale Campaign");
        assertThat(result.getStatus()).isEqualTo("PLANNING");
        assertThat(result.getBudget()).isEqualByComparingTo(new BigDecimal("5000.00"));
        assertThat(result.getSpent()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.getRevenue()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.getImpressions()).isEqualTo(0);
        assertThat(result.getClicks()).isEqualTo(0);
        assertThat(result.getLeadsGenerated()).isEqualTo(0);
        assertThat(result.getConversions()).isEqualTo(0);
        verify(campaignRepository).insert(any(Campaign.class));
    }

    @Test
    void createCampaign_withNullType_defaultsToEmail() {
        // Arrange
        CampaignRequest request = new CampaignRequest();
        request.setName("Test Campaign");
        request.setBudget(new BigDecimal("1000.00"));

        when(campaignRepository.insert(any(Campaign.class))).thenReturn(1);

        // Act
        Campaign result = campaignService.createCampaign(request);

        // Assert
        assertThat(result.getType()).isEqualTo("EMAIL");
    }

    @Test
    void activateCampaign_whenExists_setsStatusToActive() {
        // Arrange
        Long campaignId = 1L;
        Campaign campaign = new Campaign();
        campaign.setId(campaignId);
        campaign.setName("Test Campaign");
        campaign.setStatus("PLANNING");

        when(campaignRepository.selectById(campaignId)).thenReturn(campaign);
        when(campaignRepository.updateById(any(Campaign.class))).thenReturn(1);

        // Act
        Campaign result = campaignService.activateCampaign(campaignId);

        // Assert
        assertThat(result.getStatus()).isEqualTo("ACTIVE");
        assertThat(result.getStartDate()).isNotNull();
        verify(campaignRepository).updateById(any(Campaign.class));
    }

    @Test
    void activateCampaign_whenNotFound_throwsException() {
        // Arrange
        Long campaignId = 999L;
        when(campaignRepository.selectById(campaignId)).thenReturn(null);

        // Act & Assert
        assertThatThrownBy(() -> campaignService.activateCampaign(campaignId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void pauseCampaign_whenExists_setsStatusToPaused() {
        // Arrange
        Long campaignId = 1L;
        Campaign campaign = new Campaign();
        campaign.setId(campaignId);
        campaign.setStatus("ACTIVE");

        when(campaignRepository.selectById(campaignId)).thenReturn(campaign);
        when(campaignRepository.updateById(any(Campaign.class))).thenReturn(1);

        // Act
        Campaign result = campaignService.pauseCampaign(campaignId);

        // Assert
        assertThat(result.getStatus()).isEqualTo("PAUSED");
    }

    @Test
    void completeCampaign_whenExists_setsStatusToCompleted() {
        // Arrange
        Long campaignId = 1L;
        Campaign campaign = new Campaign();
        campaign.setId(campaignId);
        campaign.setStatus("ACTIVE");

        when(campaignRepository.selectById(campaignId)).thenReturn(campaign);
        when(campaignRepository.updateById(any(Campaign.class))).thenReturn(1);

        // Act
        Campaign result = campaignService.completeCampaign(campaignId);

        // Assert
        assertThat(result.getStatus()).isEqualTo("COMPLETED");
        assertThat(result.getEndDate()).isNotNull();
    }

    @Test
    void updateMetrics_withValidData_updatesAllMetrics() {
        // Arrange
        Long campaignId = 1L;
        Campaign campaign = new Campaign();
        campaign.setId(campaignId);
        campaign.setImpressions(0);
        campaign.setClicks(0);
        campaign.setLeadsGenerated(0);
        campaign.setConversions(0);

        when(campaignRepository.selectById(campaignId)).thenReturn(campaign);
        when(campaignRepository.updateById(any(Campaign.class))).thenReturn(1);

        // Act
        Campaign result = campaignService.updateMetrics(
                campaignId, 1000, 100, 50, 10, new BigDecimal("5000.00"), new BigDecimal("2000.00"));

        // Assert
        assertThat(result.getImpressions()).isEqualTo(1000);
        assertThat(result.getClicks()).isEqualTo(100);
        assertThat(result.getLeadsGenerated()).isEqualTo(50);
        assertThat(result.getConversions()).isEqualTo(10);
        assertThat(result.getRevenue()).isEqualByComparingTo(new BigDecimal("5000.00"));
        assertThat(result.getSpent()).isEqualByComparingTo(new BigDecimal("2000.00"));
    }

    @Test
    void calculateCampaignRoi_withRevenueAndSpent_calculatesCorrectly() {
        // Arrange
        Long campaignId = 1L;
        Campaign campaign = new Campaign();
        campaign.setId(campaignId);
        campaign.setRevenue(new BigDecimal("10000.00"));
        campaign.setSpent(new BigDecimal("2000.00"));

        when(campaignRepository.selectById(campaignId)).thenReturn(campaign);

        // Act
        BigDecimal roi = campaignService.calculateCampaignRoi(campaignId);

        // Assert
        // ROI = (Revenue - Spent) / Spent * 100 = (10000 - 2000) / 2000 * 100 = 400%
        assertThat(roi).isEqualByComparingTo(new BigDecimal("400.0000"));
    }

    @Test
    void calculateCampaignRoi_withZeroSpent_returnsZero() {
        // Arrange
        Long campaignId = 1L;
        Campaign campaign = new Campaign();
        campaign.setId(campaignId);
        campaign.setSpent(BigDecimal.ZERO);

        when(campaignRepository.selectById(campaignId)).thenReturn(campaign);

        // Act
        BigDecimal roi = campaignService.calculateCampaignRoi(campaignId);

        // Assert
        assertThat(roi).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void trackLead_incrementsLeadsGenerated() {
        // Arrange
        Long campaignId = 1L;
        Campaign campaign = new Campaign();
        campaign.setId(campaignId);
        campaign.setLeadsGenerated(10);

        when(campaignRepository.selectById(campaignId)).thenReturn(campaign);
        when(campaignRepository.updateById(any(Campaign.class))).thenReturn(1);

        // Act
        Campaign result = campaignService.trackLead(campaignId);

        // Assert
        assertThat(result.getLeadsGenerated()).isEqualTo(11);
    }

    @Test
    void trackConversion_incrementsConversions() {
        // Arrange
        Long campaignId = 1L;
        Campaign campaign = new Campaign();
        campaign.setId(campaignId);
        campaign.setConversions(5);

        when(campaignRepository.selectById(campaignId)).thenReturn(campaign);
        when(campaignRepository.updateById(any(Campaign.class))).thenReturn(1);

        // Act
        Campaign result = campaignService.trackConversion(campaignId);

        // Assert
        assertThat(result.getConversions()).isEqualTo(6);
    }

    @Test
    void getCampaignsByStatus_returnsFilteredList() {
        // Arrange
        Campaign campaign1 = new Campaign();
        campaign1.setStatus("ACTIVE");
        Campaign campaign2 = new Campaign();
        campaign2.setStatus("PAUSED");

        when(campaignRepository.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(List.of(campaign1));

        // Act
        List<Campaign> results = campaignService.getCampaignsByStatus("ACTIVE");

        // Assert
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getStatus()).isEqualTo("ACTIVE");
    }

    @Test
    void countByStatus_returnsCorrectCount() {
        // Arrange
        when(campaignRepository.selectCount(any(LambdaQueryWrapper.class))).thenReturn(5L);

        // Act
        long count = campaignService.countByStatus("ACTIVE");

        // Assert
        assertThat(count).isEqualTo(5L);
    }
}