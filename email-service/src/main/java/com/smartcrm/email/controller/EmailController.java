package com.smartcrm.email.controller;

import com.smartcrm.common.dto.ApiResponse;
import com.smartcrm.email.dto.EmailRequest;
import com.smartcrm.email.entity.Email;
import com.smartcrm.email.service.EmailService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Email REST controller
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/emails")
public class EmailController {

    private final EmailService emailService;

    @PostMapping("/draft")
    public ApiResponse<Email> createDraft(@Valid @RequestBody EmailRequest request) {
        // For demo, using userId = 1
        Long userId = 1L;
        Email email = emailService.createDraft(request, userId);
        return ApiResponse.success(email);
    }

    @PostMapping("/{id}/send")
    public ApiResponse<Email> sendEmail(@PathVariable Long id) {
        Email email = emailService.sendEmail(id);
        return ApiResponse.success(email);
    }

    @PutMapping("/{id}")
    public ApiResponse<Email> updateEmail(@PathVariable Long id, @Valid @RequestBody EmailRequest request) {
        Email email = emailService.updateEmail(id, request);
        return ApiResponse.success(email);
    }

    @GetMapping("/{id}")
    public ApiResponse<Email> getEmail(@PathVariable Long id) {
        Email email = emailService.getEmailById(id);
        return ApiResponse.success(email);
    }

    @GetMapping
    public ApiResponse<List<Email>> getAllEmails() {
        List<Email> emails = emailService.getAllEmails();
        return ApiResponse.success(emails);
    }

    @GetMapping("/status/{status}")
    public ApiResponse<List<Email>> getByStatus(@PathVariable String status) {
        List<Email> emails = emailService.getEmailsByStatus(status);
        return ApiResponse.success(emails);
    }

    @GetMapping("/lead/{leadId}")
    public ApiResponse<List<Email>> getByLead(@PathVariable Long leadId) {
        List<Email> emails = emailService.getEmailsByLead(leadId);
        return ApiResponse.success(emails);
    }

    @GetMapping("/contact/{contactId}")
    public ApiResponse<List<Email>> getByContact(@PathVariable Long contactId) {
        List<Email> emails = emailService.getEmailsByContact(contactId);
        return ApiResponse.success(emails);
    }

    @GetMapping("/opportunity/{opportunityId}")
    public ApiResponse<List<Email>> getByOpportunity(@PathVariable Long opportunityId) {
        List<Email> emails = emailService.getEmailsByOpportunity(opportunityId);
        return ApiResponse.success(emails);
    }

    @GetMapping("/sent/{userId}")
    public ApiResponse<List<Email>> getSentByUser(@PathVariable Long userId) {
        List<Email> emails = emailService.getSentEmailsByUser(userId);
        return ApiResponse.success(emails);
    }

    @PatchMapping("/{id}/delivered")
    public ApiResponse<Email> markAsDelivered(@PathVariable Long id) {
        Email email = emailService.markAsDelivered(id);
        return ApiResponse.success(email);
    }

    @PatchMapping("/{id}/opened")
    public ApiResponse<Email> markAsOpened(@PathVariable Long id) {
        Email email = emailService.markAsOpened(id);
        return ApiResponse.success(email);
    }

    @PatchMapping("/{id}/clicked")
    public ApiResponse<Email> markAsClicked(@PathVariable Long id) {
        Email email = emailService.markAsClicked(id);
        return ApiResponse.success(email);
    }

    @PatchMapping("/{id}/replied")
    public ApiResponse<Email> markAsReplied(@PathVariable Long id) {
        Email email = emailService.markAsReplied(id);
        return ApiResponse.success(email);
    }

    @PatchMapping("/{id}/bounced")
    public ApiResponse<Email> markAsBounced(@PathVariable Long id) {
        Email email = emailService.markAsBounced(id);
        return ApiResponse.success(email);
    }

    @PatchMapping("/{id}/failed")
    public ApiResponse<Email> markAsFailed(@PathVariable Long id) {
        Email email = emailService.markAsFailed(id);
        return ApiResponse.success(email);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteEmail(@PathVariable Long id) {
        emailService.deleteEmail(id);
        return ApiResponse.success();
    }

    @GetMapping("/stats/count-by-status")
    public ApiResponse<Map<String, Long>> countByStatus() {
        return ApiResponse.success(Map.of(
                "DRAFT", emailService.countByStatus("DRAFT"),
                "SENT", emailService.countByStatus("SENT"),
                "DELIVERED", emailService.countByStatus("DELIVERED"),
                "OPENED", emailService.countByStatus("OPENED"),
                "CLICKED", emailService.countByStatus("CLICKED"),
                "REPLIED", emailService.countByStatus("REPLIED"),
                "BOUNCED", emailService.countByStatus("BOUNCED"),
                "FAILED", emailService.countByStatus("FAILED")
        ));
    }

    @GetMapping("/stats/ai-generated-count")
    public ApiResponse<Long> countAiGenerated() {
        return ApiResponse.success(emailService.countAiGenerated());
    }
}
