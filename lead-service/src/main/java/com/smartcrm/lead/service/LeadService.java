package com.smartcrm.lead.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartcrm.common.exception.ResourceNotFoundException;
import com.smartcrm.lead.dto.LeadRequest;
import com.smartcrm.lead.entity.Lead;
import com.smartcrm.lead.repository.LeadRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Lead service - manages sales leads through the pipeline
 */
@Slf4j
@Service
public class LeadService extends ServiceImpl<LeadRepository, Lead> {

    public Lead createLead(LeadRequest request) {
        log.info("Creating lead: {} {}", request.getFirstName(), request.getLastName());

        Lead lead = new Lead();
        lead.setFirstName(request.getFirstName());
        lead.setLastName(request.getLastName());
        lead.setEmail(request.getEmail());
        lead.setPhone(request.getPhone());
        lead.setCompany(request.getCompany());
        lead.setTitle(request.getTitle());
        lead.setSource(request.getSource() != null ? request.getSource() : "WEB");
        lead.setStatus("NEW");
        lead.setRating("COLD");
        lead.setOwnerId(request.getOwnerId());
        lead.setNotes(request.getNotes());
        lead.setCreatedAt(LocalDateTime.now());
        lead.setUpdatedAt(LocalDateTime.now());

        this.save(lead);
        log.info("Lead created with ID: {}", lead.getId());
        return lead;
    }

    public Lead updateLead(Long id, LeadRequest request) {
        Lead existing = this.getById(id);
        if (existing == null) {
            throw new ResourceNotFoundException("Lead", id);
        }

        existing.setFirstName(request.getFirstName());
        existing.setLastName(request.getLastName());
        existing.setEmail(request.getEmail());
        existing.setPhone(request.getPhone());
        existing.setCompany(request.getCompany());
        existing.setTitle(request.getTitle());
        existing.setSource(request.getSource());
        existing.setOwnerId(request.getOwnerId());
        existing.setNotes(request.getNotes());
        existing.setUpdatedAt(LocalDateTime.now());

        this.updateById(existing);
        return existing;
    }

    public Lead getLeadById(Long id) {
        Lead lead = this.getById(id);
        if (lead == null) {
            throw new ResourceNotFoundException("Lead", id);
        }
        return lead;
    }

    public List<Lead> getAllLeads() {
        return this.list();
    }

    public List<Lead> getLeadsByStatus(String status) {
        return this.list(new LambdaQueryWrapper<Lead>().eq(Lead::getStatus, status));
    }

    public List<Lead> getLeadsByOwner(Long ownerId) {
        return this.list(new LambdaQueryWrapper<Lead>().eq(Lead::getOwnerId, ownerId));
    }

    public List<Lead> getLeadsByRating(String rating) {
        return this.list(new LambdaQueryWrapper<Lead>().eq(Lead::getRating, rating));
    }

    public List<Lead> getHotLeads() {
        return this.list(new LambdaQueryWrapper<Lead>()
                .eq(Lead::getRating, "HOT")
                .ne(Lead::getStatus, "CONVERTED")
                .ne(Lead::getStatus, "UNQUALIFIED"));
    }

    public Lead updateLeadStatus(Long id, String status) {
        Lead lead = this.getById(id);
        if (lead == null) {
            throw new ResourceNotFoundException("Lead", id);
        }
        lead.setStatus(status);
        lead.setUpdatedAt(LocalDateTime.now());
        this.updateById(lead);
        return lead;
    }

    public Lead updateLeadRating(Long id, String rating) {
        Lead lead = this.getById(id);
        if (lead == null) {
            throw new ResourceNotFoundException("Lead", id);
        }
        lead.setRating(rating);
        lead.setUpdatedAt(LocalDateTime.now());
        this.updateById(lead);
        return lead;
    }

    public Lead scheduleFollowUp(Long id, LocalDateTime followUpDate) {
        Lead lead = this.getById(id);
        if (lead == null) {
            throw new ResourceNotFoundException("Lead", id);
        }
        lead.setNextFollowUpDate(followUpDate);
        lead.setLastContactDate(LocalDateTime.now());
        lead.setUpdatedAt(LocalDateTime.now());
        this.updateById(lead);
        return lead;
    }

    public Lead convertLead(Long id, Long accountId, Long contactId) {
        Lead lead = this.getById(id);
        if (lead == null) {
            throw new ResourceNotFoundException("Lead", id);
        }
        lead.setStatus("CONVERTED");
        lead.setConvertedAccountId(accountId);
        lead.setConvertedContactId(contactId);
        lead.setConvertedAt(LocalDateTime.now());
        lead.setUpdatedAt(LocalDateTime.now());
        this.updateById(lead);
        log.info("Lead {} converted to Account {} and Contact {}", id, accountId, contactId);
        return lead;
    }

    public Lead updateAiScore(Long id, String aiScore, Double aiScoreValue) {
        Lead lead = this.getById(id);
        if (lead == null) {
            throw new ResourceNotFoundException("Lead", id);
        }
        lead.setAiScore(aiScore);
        lead.setAiScoreValue(aiScoreValue);
        lead.setUpdatedAt(LocalDateTime.now());
        this.updateById(lead);
        return lead;
    }

    public void deleteLead(Long id) {
        this.removeById(id);
    }

    public long countLeadsByStatus(String status) {
        return this.count(new LambdaQueryWrapper<Lead>().eq(Lead::getStatus, status));
    }

    public long countHotLeads() {
        return this.count(new LambdaQueryWrapper<Lead>()
                .eq(Lead::getRating, "HOT")
                .ne(Lead::getStatus, "CONVERTED"));
    }
}
