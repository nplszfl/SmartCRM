package com.smartcrm.crm.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartcrm.common.exception.ResourceNotFoundException;
import com.smartcrm.crm.dto.CampaignRequest;
import com.smartcrm.crm.entity.Campaign;
import com.smartcrm.crm.repository.CampaignRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Campaign service - manages marketing campaigns
 */
@Slf4j
@Service
public class CampaignService extends ServiceImpl<CampaignRepository, Campaign> {

    public Campaign createCampaign(CampaignRequest request) {
        log.info("Creating campaign: {}", request.getName());

        Campaign campaign = new Campaign();
        campaign.setName(request.getName());
        campaign.setType(request.getType() != null ? request.getType() : "EMAIL");
        campaign.setStatus("PLANNING");
        campaign.setDescription(request.getDescription());
        campaign.setStartDate(request.getStartDate());
        campaign.setEndDate(request.getEndDate());
        campaign.setBudget(request.getBudget());
        campaign.setSpent(BigDecimal.ZERO);
        campaign.setCurrency(request.getCurrency() != null ? request.getCurrency() : "USD");
        campaign.setOwnerId(request.getOwnerId());
        campaign.setOwnerName(request.getOwnerName());
        campaign.setTargetLeads(request.getTargetLeads() != null ? request.getTargetLeads() : 0);
        campaign.setTargetConversions(request.getTargetConversions() != null ? request.getTargetConversions() : 0);
        campaign.setTargetRevenue(request.getTargetRevenue() != null ? request.getTargetRevenue() : 0);
        campaign.setImpressions(0);
        campaign.setClicks(0);
        campaign.setLeadsGenerated(0);
        campaign.setConversions(0);
        campaign.setRevenue(BigDecimal.ZERO);
        campaign.setChannel(request.getChannel());
        campaign.setTargetAudience(request.getTargetAudience());
        campaign.setObjective(request.getObjective());
        campaign.setCampaignCode(request.getCampaignCode());
        campaign.setCreatedAt(LocalDateTime.now());
        campaign.setUpdatedAt(LocalDateTime.now());

        this.save(campaign);
        log.info("Campaign created with ID: {}", campaign.getId());
        return campaign;
    }

    public Campaign updateCampaign(Long id, CampaignRequest request) {
        Campaign existing = this.getById(id);
        if (existing == null) {
            throw new ResourceNotFoundException("Campaign", id);
        }

        existing.setName(request.getName());
        existing.setType(request.getType());
        existing.setDescription(request.getDescription());
        existing.setStartDate(request.getStartDate());
        existing.setEndDate(request.getEndDate());
        existing.setBudget(request.getBudget());
        existing.setCurrency(request.getCurrency());
        existing.setOwnerId(request.getOwnerId());
        existing.setOwnerName(request.getOwnerName());
        existing.setTargetLeads(request.getTargetLeads());
        existing.setTargetConversions(request.getTargetConversions());
        existing.setTargetRevenue(request.getTargetRevenue());
        existing.setChannel(request.getChannel());
        existing.setTargetAudience(request.getTargetAudience());
        existing.setObjective(request.getObjective());
        existing.setCampaignCode(request.getCampaignCode());
        existing.setUpdatedAt(LocalDateTime.now());

        this.updateById(existing);
        return existing;
    }

    public Campaign getCampaignById(Long id) {
        Campaign campaign = this.getById(id);
        if (campaign == null) {
            throw new ResourceNotFoundException("Campaign", id);
        }
        return campaign;
    }

    public List<Campaign> getAllCampaigns() {
        return this.list();
    }

    public List<Campaign> getCampaignsByStatus(String status) {
        return this.list(new LambdaQueryWrapper<Campaign>().eq(Campaign::getStatus, status));
    }

    public List<Campaign> getCampaignsByType(String type) {
        return this.list(new LambdaQueryWrapper<Campaign>().eq(Campaign::getType, type));
    }

    public List<Campaign> getCampaignsByOwner(Long ownerId) {
        return this.list(new LambdaQueryWrapper<Campaign>().eq(Campaign::getOwnerId, ownerId));
    }

    public List<Campaign> getActiveCampaigns() {
        return this.list(new LambdaQueryWrapper<Campaign>()
                .eq(Campaign::getStatus, "ACTIVE")
                .orderByDesc(Campaign::getStartDate));
    }

    public Campaign activateCampaign(Long id) {
        Campaign campaign = this.getById(id);
        if (campaign == null) {
            throw new ResourceNotFoundException("Campaign", id);
        }
        campaign.setStatus("ACTIVE");
        campaign.setStartDate(LocalDateTime.now());
        campaign.setUpdatedAt(LocalDateTime.now());
        this.updateById(campaign);
        log.info("Campaign {} activated", id);
        return campaign;
    }

    public Campaign pauseCampaign(Long id) {
        Campaign campaign = this.getById(id);
        if (campaign == null) {
            throw new ResourceNotFoundException("Campaign", id);
        }
        campaign.setStatus("PAUSED");
        campaign.setUpdatedAt(LocalDateTime.now());
        this.updateById(campaign);
        log.info("Campaign {} paused", id);
        return campaign;
    }

    public Campaign completeCampaign(Long id) {
        Campaign campaign = this.getById(id);
        if (campaign == null) {
            throw new ResourceNotFoundException("Campaign", id);
        }
        campaign.setStatus("COMPLETED");
        campaign.setEndDate(LocalDateTime.now());
        campaign.setUpdatedAt(LocalDateTime.now());
        this.updateById(campaign);
        log.info("Campaign {} completed", id);
        return campaign;
    }

    public Campaign updateMetrics(Long id, Integer impressions, Integer clicks, 
                                   Integer leadsGenerated, Integer conversions, 
                                   BigDecimal revenue, BigDecimal spent) {
        Campaign campaign = this.getById(id);
        if (campaign == null) {
            throw new ResourceNotFoundException("Campaign", id);
        }

        if (impressions != null) campaign.setImpressions(impressions);
        if (clicks != null) campaign.setClicks(clicks);
        if (leadsGenerated != null) campaign.setLeadsGenerated(leadsGenerated);
        if (conversions != null) campaign.setConversions(conversions);
        if (revenue != null) campaign.setRevenue(revenue);
        if (spent != null) campaign.setSpent(spent);
        campaign.setUpdatedAt(LocalDateTime.now());

        this.updateById(campaign);
        return campaign;
    }

    public void deleteCampaign(Long id) {
        this.removeById(id);
    }

    public BigDecimal getTotalBudget() {
        return this.list().stream()
                .map(Campaign::getBudget)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getTotalSpent() {
        return this.list().stream()
                .map(Campaign::getSpent)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getTotalRevenue() {
        return this.list().stream()
                .map(Campaign::getRevenue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal calculateCampaignRoi(Long id) {
        Campaign campaign = this.getById(id);
        if (campaign == null || campaign.getSpent() == null || campaign.getSpent().compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return campaign.getRevenue()
                .subtract(campaign.getSpent())
                .divide(campaign.getSpent(), 4, java.math.RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }

    public Campaign trackLead(Long id) {
        Campaign campaign = this.getById(id);
        if (campaign == null) {
            throw new ResourceNotFoundException("Campaign", id);
        }
        campaign.setLeadsGenerated(campaign.getLeadsGenerated() + 1);
        campaign.setUpdatedAt(LocalDateTime.now());
        this.updateById(campaign);
        return campaign;
    }

    public Campaign trackConversion(Long id) {
        Campaign campaign = this.getById(id);
        if (campaign == null) {
            throw new ResourceNotFoundException("Campaign", id);
        }
        campaign.setConversions(campaign.getConversions() + 1);
        campaign.setUpdatedAt(LocalDateTime.now());
        this.updateById(campaign);
        return campaign;
    }

    public long countByStatus(String status) {
        return this.count(new LambdaQueryWrapper<Campaign>().eq(Campaign::getStatus, status));
    }

    public Campaign addAiRecommendation(Long id, String recommendation, Double predictedRoi) {
        Campaign campaign = this.getById(id);
        if (campaign == null) {
            throw new ResourceNotFoundException("Campaign", id);
        }
        campaign.setAiRecommendation(recommendation);
        campaign.setPredictedRoi(predictedRoi);
        campaign.setUpdatedAt(LocalDateTime.now());
        this.updateById(campaign);
        return campaign;
    }
}
