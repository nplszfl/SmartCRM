package com.smartcrm.email.controller;

import com.smartcrm.common.dto.ApiResponse;
import com.smartcrm.email.dto.EmailTemplateRequest;
import com.smartcrm.email.entity.EmailTemplate;
import com.smartcrm.email.service.EmailTemplateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * EmailTemplate REST controller
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/email-templates")
public class EmailTemplateController {

    private final EmailTemplateService emailTemplateService;

    @PostMapping
    public ApiResponse<EmailTemplate> createTemplate(@Valid @RequestBody EmailTemplateRequest request) {
        EmailTemplate template = emailTemplateService.createTemplate(request);
        return ApiResponse.success(template);
    }

    @GetMapping("/{id}")
    public ApiResponse<EmailTemplate> getTemplate(@PathVariable Long id) {
        EmailTemplate template = emailTemplateService.getTemplateById(id);
        return ApiResponse.success(template);
    }

    @GetMapping
    public ApiResponse<List<EmailTemplate>> getAllTemplates() {
        List<EmailTemplate> templates = emailTemplateService.getAllTemplates();
        return ApiResponse.success(templates);
    }

    @GetMapping("/active")
    public ApiResponse<List<EmailTemplate>> getActiveTemplates() {
        List<EmailTemplate> templates = emailTemplateService.getActiveTemplates();
        return ApiResponse.success(templates);
    }

    @GetMapping("/type/{type}")
    public ApiResponse<List<EmailTemplate>> getTemplatesByType(@PathVariable String type) {
        List<EmailTemplate> templates = emailTemplateService.getTemplatesByType(type);
        return ApiResponse.success(templates);
    }

    @PutMapping("/{id}")
    public ApiResponse<EmailTemplate> updateTemplate(@PathVariable Long id, @Valid @RequestBody EmailTemplateRequest request) {
        EmailTemplate template = emailTemplateService.updateTemplate(id, request);
        return ApiResponse.success(template);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteTemplate(@PathVariable Long id) {
        emailTemplateService.deleteTemplate(id);
        return ApiResponse.success();
    }

    @PostMapping("/{id}/preview")
    public ApiResponse<Map<String, String>> previewTemplate(@PathVariable Long id, @RequestBody Map<String, String> variables) {
        Map<String, String> preview = emailTemplateService.generateFromTemplate(id, variables);
        return ApiResponse.success(preview);
    }
}