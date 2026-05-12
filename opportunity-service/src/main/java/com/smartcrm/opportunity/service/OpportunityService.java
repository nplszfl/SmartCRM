package com.smartcrm.opportunity.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartcrm.common.exception.ResourceNotFoundException;
import com.smartcrm.opportunity.dto.OpportunityRequest;
import com.smartcrm.opportunity.entity.Opportunity;
import com.smartcrm.opportunity.repository.OpportunityRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Opportunity service - manages sales opportunities
 */
@Slf4j
@Service
public class OpportunityService extends ServiceImpl<OpportunityRepository, Opportunity> {

    public Opportunity createOpportunity(OpportunityRequest request) {
        log.info("Creating opportunity: {}", request.getName());

        Opportunity opp = new Opportunity();
        opp.setName(request.getName());
        opp.setDescription(request.getDescription());
        opp.setAccountId(request.getAccountId());
        opp.setContactId(request.getContactId());
        opp.setLeadId(request.getLeadId());
        opp.setStage(request.getStage() != null ? request.getStage() : "PROSPECTING");
        opp.setAmount(request.getAmount());
        opp.setProbability(request.getProbability() != null ? request.getProbability() : calculateDefaultProbability(request.getStage()));
        opp.setOwnerId(request.getOwnerId());
        if (request.getExpectedCloseDate() != null) {
            opp.setExpectedCloseDate(LocalDate.parse(request.getExpectedCloseDate()));
        }
        opp.setType(request.getType() != null ? request.getType() : "NEW_BUSINESS");
        opp.setSource(request.getSource());
        opp.setCreatedAt(LocalDateTime.now());
        opp.setUpdatedAt(LocalDateTime.now());

        this.save(opp);
        log.info("Opportunity created with ID: {}", opp.getId());
        return opp;
    }

    public Opportunity updateOpportunity(Long id, OpportunityRequest request) {
        Opportunity existing = this.getById(id);
        if (existing == null) {
            throw new ResourceNotFoundException("Opportunity", id);
        }

        existing.setName(request.getName());
        existing.setDescription(request.getDescription());
        existing.setAccountId(request.getAccountId());
        existing.setContactId(request.getContactId());
        existing.setLeadId(request.getLeadId());
        if (request.getStage() != null) {
            existing.setStage(request.getStage());
            // Update probability based on stage
            existing.setProbability(calculateDefaultProbability(request.getStage()));
        }
        existing.setAmount(request.getAmount());
        existing.setOwnerId(request.getOwnerId());
        if (request.getExpectedCloseDate() != null) {
            existing.setExpectedCloseDate(LocalDate.parse(request.getExpectedCloseDate()));
        }
        existing.setType(request.getType());
        existing.setUpdatedAt(LocalDateTime.now());

        this.updateById(existing);
        return existing;
    }

    public Opportunity getOpportunityById(Long id) {
        Opportunity opp = this.getById(id);
        if (opp == null) {
            throw new ResourceNotFoundException("Opportunity", id);
        }
        return opp;
    }

    public List<Opportunity> getAllOpportunities() {
        return this.list();
    }

    public List<Opportunity> getOpportunitiesByStage(String stage) {
        return this.list(new LambdaQueryWrapper<Opportunity>().eq(Opportunity::getStage, stage));
    }

    public List<Opportunity> getOpportunitiesByOwner(Long ownerId) {
        return this.list(new LambdaQueryWrapper<Opportunity>().eq(Opportunity::getOwnerId, ownerId));
    }

    public List<Opportunity> getOpportunitiesByAccount(Long accountId) {
        return this.list(new LambdaQueryWrapper<Opportunity>().eq(Opportunity::getAccountId, accountId));
    }

    public Opportunity moveToStage(Long id, String newStage) {
        Opportunity opp = this.getById(id);
        if (opp == null) {
            throw new ResourceNotFoundException("Opportunity", id);
        }
        opp.setStage(newStage);
        opp.setProbability(calculateDefaultProbability(newStage));
        opp.setUpdatedAt(LocalDateTime.now());
        this.updateById(opp);
        log.info("Opportunity {} moved to stage {}", id, newStage);
        return opp;
    }

    public Opportunity closeAsWon(Long id) {
        Opportunity opp = this.getById(id);
        if (opp == null) {
            throw new ResourceNotFoundException("Opportunity", id);
        }
        opp.setStage("CLOSED_WON");
        opp.setProbability(100.0);
        opp.setActualCloseDate(LocalDateTime.now());
        opp.setUpdatedAt(LocalDateTime.now());
        this.updateById(opp);
        log.info("Opportunity {} closed as WON", id);
        return opp;
    }

    public Opportunity closeAsLost(Long id) {
        Opportunity opp = this.getById(id);
        if (opp == null) {
            throw new ResourceNotFoundException("Opportunity", id);
        }
        opp.setStage("CLOSED_LOST");
        opp.setProbability(0.0);
        opp.setActualCloseDate(LocalDateTime.now());
        opp.setUpdatedAt(LocalDateTime.now());
        this.updateById(opp);
        log.info("Opportunity {} closed as LOST", id);
        return opp;
    }

    public Opportunity updateAiPrediction(Long id, String prediction, Double confidence) {
        Opportunity opp = this.getById(id);
        if (opp == null) {
            throw new ResourceNotFoundException("Opportunity", id);
        }
        opp.setAiPrediction(prediction);
        opp.setAiConfidenceScore(confidence);
        opp.setUpdatedAt(LocalDateTime.now());
        this.updateById(opp);
        return opp;
    }

    public void deleteOpportunity(Long id) {
        this.removeById(id);
    }

    public BigDecimal getTotalPipelineValue() {
        List<Opportunity> openOpps = this.list(new LambdaQueryWrapper<Opportunity>()
                .notIn(Opportunity::getStage, "CLOSED_WON", "CLOSED_LOST"));
        return openOpps.stream()
                .map(Opportunity::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getWeightedPipelineValue() {
        List<Opportunity> openOpps = this.list(new LambdaQueryWrapper<Opportunity>()
                .notIn(Opportunity::getStage, "CLOSED_WON", "CLOSED_LOST"));
        return openOpps.stream()
                .map(opp -> opp.getAmount().multiply(BigDecimal.valueOf(opp.getProbability() / 100.0)))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public long countByStage(String stage) {
        return this.count(new LambdaQueryWrapper<Opportunity>().eq(Opportunity::getStage, stage));
    }

    private BigDecimal calculateDefaultProbability(String stage) {
        return switch (stage) {
            case "PROSPECTING" -> new BigDecimal("10");
            case "QUALIFICATION" -> new BigDecimal("25");
            case "PROPOSAL" -> new BigDecimal("50");
            case "NEGOTIATION" -> new BigDecimal("75");
            case "CLOSED_WON" -> new BigDecimal("100");
            case "CLOSED_LOST" -> new BigDecimal("0");
            default -> new BigDecimal("10");
        };
    }
}
