package com.smartcrm.email.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartcrm.common.exception.ResourceNotFoundException;
import com.smartcrm.email.dto.EmailTemplateRequest;
import com.smartcrm.email.entity.EmailTemplate;
import com.smartcrm.email.repository.EmailTemplateRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.HashMap;

/**
 * EmailTemplate service - manages email templates
 */
@Slf4j
@Service
public class EmailTemplateService extends ServiceImpl<EmailTemplateRepository, EmailTemplate> {

    public EmailTemplate createTemplate(EmailTemplateRequest request) {
        log.info("Creating email template: {}", request.getName());

        EmailTemplate template = new EmailTemplate();
        template.setName(request.getName());
        template.setSubject(request.getSubject());
        template.setBody(request.getBody());
        template.setHtmlBody(request.getHtmlBody());
        template.setType(request.getType() != null ? request.getType() : "OUTBOUND");
        template.setDescription(request.getDescription());
        template.setActive(request.getActive() != null ? request.getActive() : true);
        template.setCreatedAt(LocalDateTime.now());
        template.setUpdatedAt(LocalDateTime.now());

        this.save(template);
        log.info("Email template created with ID: {}", template.getId());
        return template;
    }

    public EmailTemplate getTemplateById(Long id) {
        EmailTemplate template = this.getById(id);
        if (template == null) {
            throw new ResourceNotFoundException("EmailTemplate", id);
        }
        return template;
    }

    public List<EmailTemplate> getAllTemplates() {
        return this.list();
    }

    public List<EmailTemplate> getActiveTemplates() {
        return this.list(new LambdaQueryWrapper<EmailTemplate>().eq(EmailTemplate::getActive, true));
    }

    public List<EmailTemplate> getTemplatesByType(String type) {
        return this.list(new LambdaQueryWrapper<EmailTemplate>().eq(EmailTemplate::getType, type));
    }

    public EmailTemplate updateTemplate(Long id, EmailTemplateRequest request) {
        EmailTemplate existing = this.getById(id);
        if (existing == null) {
            throw new ResourceNotFoundException("EmailTemplate", id);
        }

        if (request.getName() != null) {
            existing.setName(request.getName());
        }
        if (request.getSubject() != null) {
            existing.setSubject(request.getSubject());
        }
        if (request.getBody() != null) {
            existing.setBody(request.getBody());
        }
        if (request.getHtmlBody() != null) {
            existing.setHtmlBody(request.getHtmlBody());
        }
        if (request.getType() != null) {
            existing.setType(request.getType());
        }
        if (request.getDescription() != null) {
            existing.setDescription(request.getDescription());
        }
        if (request.getActive() != null) {
            existing.setActive(request.getActive());
        }
        existing.setUpdatedAt(LocalDateTime.now());

        this.updateById(existing);
        return existing;
    }

    public void deleteTemplate(Long id) {
        EmailTemplate template = this.getById(id);
        if (template == null) {
            throw new ResourceNotFoundException("EmailTemplate", id);
        }
        this.removeById(id);
    }

    /**
     * Apply template variables to template content.
     * Supports {{variable}} placeholder syntax.
     */
    public String applyTemplate(String templateContent, Map<String, String> variables) {
        if (templateContent == null || templateContent.isEmpty()) {
            return templateContent;
        }
        if (variables == null || variables.isEmpty()) {
            return templateContent;
        }

        String result = templateContent;
        Pattern pattern = Pattern.compile("\\{\\{(\\w+)\\}\\}");
        java.util.regex.Matcher matcher = pattern.matcher(templateContent);

        while (matcher.find()) {
            String variableName = matcher.group(1);
            String replacement = variables.getOrDefault(variableName, "{{" + variableName + "}}");
            result = result.replace("{{" + variableName + "}}", replacement);
        }

        return result;
    }

    /**
     * Generate email content from a template with the given variables.
     */
    public Map<String, String> generateFromTemplate(Long templateId, Map<String, String> variables) {
        EmailTemplate template = getTemplateById(templateId);

        Map<String, String> result = new HashMap<>();
        result.put("subject", applyTemplate(template.getSubject(), variables));
        result.put("body", applyTemplate(template.getBody(), variables));
        if (template.getHtmlBody() != null) {
            result.put("htmlBody", applyTemplate(template.getHtmlBody(), variables));
        }

        return result;
    }

    public long countByActive(Boolean active) {
        return this.count(new LambdaQueryWrapper<EmailTemplate>().eq(EmailTemplate::getActive, active));
    }
}