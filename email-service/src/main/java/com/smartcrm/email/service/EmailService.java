package com.smartcrm.email.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartcrm.common.exception.ResourceNotFoundException;
import com.smartcrm.email.dto.EmailRequest;
import com.smartcrm.email.entity.Email;
import com.smartcrm.email.repository.EmailRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Email service - manages emails in the CRM
 */
@Slf4j
@Service
public class EmailService extends ServiceImpl<EmailRepository, Email> {

    public Email createDraft(EmailRequest request, Long userId) {
        log.info("Creating email draft for user {}", userId);

        Email email = new Email();
        email.setLeadId(request.getLeadId());
        email.setContactId(request.getContactId());
        email.setOpportunityId(request.getOpportunityId());
        email.setFromAddress(getUserEmail(userId));
        email.setToAddress(request.getToAddress());
        email.setCcAddress(request.getCcAddress());
        email.setBccAddress(request.getBccAddress());
        email.setSubject(request.getSubject());
        email.setBody(request.getBody());
        email.setHtmlBody(request.getHtmlBody());
        email.setStatus("DRAFT");
        email.setType(request.getType() != null ? request.getType() : "OUTBOUND");
        email.setSentBy(userId);
        email.setAiGenerated(request.getAiPromptUsed() != null ? "YES" : "NO");
        email.setAiPromptUsed(request.getAiPromptUsed());
        email.setTemplateId(request.getTemplateId());
        email.setCreatedAt(LocalDateTime.now());
        email.setUpdatedAt(LocalDateTime.now());

        this.save(email);
        log.info("Email draft created with ID: {}", email.getId());
        return email;
    }

    public Email sendEmail(Long id) {
        Email email = this.getById(id);
        if (email == null) {
            throw new ResourceNotFoundException("Email", id);
        }
        if (!"DRAFT".equals(email.getStatus())) {
            throw new IllegalStateException("Only draft emails can be sent. Current status: " + email.getStatus());
        }

        email.setStatus("SENT");
        email.setSentAt(LocalDateTime.now());
        email.setUpdatedAt(LocalDateTime.now());
        this.updateById(email);

        log.info("Email {} sent to {}", id, email.getToAddress());
        return email;
    }

    public Email updateEmail(Long id, EmailRequest request) {
        Email existing = this.getById(id);
        if (existing == null) {
            throw new ResourceNotFoundException("Email", id);
        }

        existing.setToAddress(request.getToAddress());
        existing.setCcAddress(request.getCcAddress());
        existing.setBccAddress(request.getBccAddress());
        existing.setSubject(request.getSubject());
        existing.setBody(request.getBody());
        existing.setHtmlBody(request.getHtmlBody());
        existing.setUpdatedAt(LocalDateTime.now());

        this.updateById(existing);
        return existing;
    }

    public Email getEmailById(Long id) {
        Email email = this.getById(id);
        if (email == null) {
            throw new ResourceNotFoundException("Email", id);
        }
        return email;
    }

    public List<Email> getAllEmails() {
        return this.list();
    }

    public List<Email> getEmailsByStatus(String status) {
        return this.list(new LambdaQueryWrapper<Email>().eq(Email::getStatus, status));
    }

    public List<Email> getEmailsByLead(Long leadId) {
        return this.list(new LambdaQueryWrapper<Email>().eq(Email::getLeadId, leadId));
    }

    public List<Email> getEmailsByContact(Long contactId) {
        return this.list(new LambdaQueryWrapper<Email>().eq(Email::getContactId, contactId));
    }

    public List<Email> getEmailsByOpportunity(Long opportunityId) {
        return this.list(new LambdaQueryWrapper<Email>().eq(Email::getOpportunityId, opportunityId));
    }

    public List<Email> getSentEmailsByUser(Long userId) {
        return this.list(new LambdaQueryWrapper<Email>()
                .eq(Email::getSentBy, userId)
                .eq(Email::getType, "OUTBOUND")
                .orderByDesc(Email::getSentAt));
    }

    public Email markAsDelivered(Long id) {
        Email email = this.getById(id);
        if (email == null) {
            throw new ResourceNotFoundException("Email", id);
        }
        email.setStatus("DELIVERED");
        email.setDeliveredAt(LocalDateTime.now());
        email.setUpdatedAt(LocalDateTime.now());
        this.updateById(email);
        return email;
    }

    public Email markAsOpened(Long id) {
        Email email = this.getById(id);
        if (email == null) {
            throw new ResourceNotFoundException("Email", id);
        }
        email.setStatus("OPENED");
        email.setOpenedAt(LocalDateTime.now());
        email.setUpdatedAt(LocalDateTime.now());
        this.updateById(email);
        return email;
    }

    public Email markAsClicked(Long id) {
        Email email = this.getById(id);
        if (email == null) {
            throw new ResourceNotFoundException("Email", id);
        }
        email.setStatus("CLICKED");
        email.setClickedAt(LocalDateTime.now());
        email.setUpdatedAt(LocalDateTime.now());
        this.updateById(email);
        return email;
    }

    public Email markAsReplied(Long id) {
        Email email = this.getById(id);
        if (email == null) {
            throw new ResourceNotFoundException("Email", id);
        }
        email.setStatus("REPLIED");
        email.setRepliedAt(LocalDateTime.now());
        email.setUpdatedAt(LocalDateTime.now());
        this.updateById(email);
        return email;
    }

    public Email markAsBounced(Long id) {
        Email email = this.getById(id);
        if (email == null) {
            throw new ResourceNotFoundException("Email", id);
        }
        email.setStatus("BOUNCED");
        email.setUpdatedAt(LocalDateTime.now());
        this.updateById(email);
        return email;
    }

    public Email markAsFailed(Long id) {
        Email email = this.getById(id);
        if (email == null) {
            throw new ResourceNotFoundException("Email", id);
        }
        email.setStatus("FAILED");
        email.setUpdatedAt(LocalDateTime.now());
        this.updateById(email);
        return email;
    }

    public void deleteEmail(Long id) {
        this.removeById(id);
    }

    public long countByStatus(String status) {
        return this.count(new LambdaQueryWrapper<Email>().eq(Email::getStatus, status));
    }

    public long countAiGenerated() {
        return this.count(new LambdaQueryWrapper<Email>().eq(Email::getAiGenerated, "YES"));
    }

    private String getUserEmail(Long userId) {
        // In real implementation, would fetch from user service
        return "user" + userId + "@company.com";
    }
}
