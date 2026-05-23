package com.smartcrm.lead.controller;

import com.smartcrm.common.dto.ApiResponse;
import com.smartcrm.common.dto.PageResponse;
import com.smartcrm.lead.dto.LeadRequest;
import com.smartcrm.lead.entity.Lead;
import com.smartcrm.lead.service.LeadService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Lead REST controller
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/leads")
public class LeadController {

    private final LeadService leadService;

    @PostMapping
    public ApiResponse<Lead> createLead(@Valid @RequestBody LeadRequest request) {
        Lead lead = leadService.createLead(request);
        return ApiResponse.success(lead);
    }

    @PutMapping("/{id}")
    public ApiResponse<Lead> updateLead(@PathVariable Long id, @Valid @RequestBody LeadRequest request) {
        Lead lead = leadService.updateLead(id, request);
        return ApiResponse.success(lead);
    }

    @GetMapping("/{id}")
    public ApiResponse<Lead> getLead(@PathVariable Long id) {
        Lead lead = leadService.getLeadById(id);
        return ApiResponse.success(lead);
    }

    @GetMapping
    public ApiResponse<List<Lead>> getAllLeads() {
        List<Lead> leads = leadService.getAllLeads();
        return ApiResponse.success(leads);
    }

    @GetMapping("/page")
    public ApiResponse<PageResponse<Lead>> getAllLeadsPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageResponse<Lead> pageResp = leadService.getAllLeadsPage(page, size);
        return ApiResponse.success(pageResp);
    }

    @GetMapping("/status/{status}")
    public ApiResponse<List<Lead>> getLeadsByStatus(@PathVariable String status) {
        List<Lead> leads = leadService.getLeadsByStatus(status);
        return ApiResponse.success(leads);
    }

    @GetMapping("/owner/{ownerId}")
    public ApiResponse<List<Lead>> getLeadsByOwner(@PathVariable Long ownerId) {
        List<Lead> leads = leadService.getLeadsByOwner(ownerId);
        return ApiResponse.success(leads);
    }

    @GetMapping("/rating/{rating}")
    public ApiResponse<List<Lead>> getLeadsByRating(@PathVariable String rating) {
        List<Lead> leads = leadService.getLeadsByRating(rating);
        return ApiResponse.success(leads);
    }

    @GetMapping("/hot")
    public ApiResponse<List<Lead>> getHotLeads() {
        List<Lead> leads = leadService.getHotLeads();
        return ApiResponse.success(leads);
    }

    @PatchMapping("/{id}/status")
    public ApiResponse<Lead> updateLeadStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String status = body.get("status");
        Lead lead = leadService.updateLeadStatus(id, status);
        return ApiResponse.success(lead);
    }

    @PatchMapping("/{id}/rating")
    public ApiResponse<Lead> updateLeadRating(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String rating = body.get("rating");
        Lead lead = leadService.updateLeadRating(id, rating);
        return ApiResponse.success(lead);
    }

    @PatchMapping("/{id}/followup")
    public ApiResponse<Lead> scheduleFollowUp(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        LocalDateTime followUpDate = LocalDateTime.parse(body.get("followUpDate"));
        Lead lead = leadService.scheduleFollowUp(id, followUpDate);
        return ApiResponse.success(lead);
    }

    @PostMapping("/{id}/convert")
    public ApiResponse<Lead> convertLead(@PathVariable Long id, @RequestBody Map<String, Long> body) {
        Long accountId = body.get("accountId");
        Long contactId = body.get("contactId");
        Lead lead = leadService.convertLead(id, accountId, contactId);
        return ApiResponse.success(lead);
    }

    @PatchMapping("/{id}/ai-score")
    public ApiResponse<Lead> updateAiScore(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        String aiScore = (String) body.get("aiScore");
        Double aiScoreValue = ((Number) body.get("aiScoreValue")).doubleValue();
        Lead lead = leadService.updateAiScore(id, aiScore, aiScoreValue);
        return ApiResponse.success(lead);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteLead(@PathVariable Long id) {
        leadService.deleteLead(id);
        return ApiResponse.success();
    }

    @GetMapping("/stats/count-by-status")
    public ApiResponse<Map<String, Long>> countByStatus() {
        return ApiResponse.success(Map.of(
                "NEW", leadService.countLeadsByStatus("NEW"),
                "CONTACTED", leadService.countLeadsByStatus("CONTACTED"),
                "QUALIFIED", leadService.countLeadsByStatus("QUALIFIED"),
                "UNQUALIFIED", leadService.countLeadsByStatus("UNQUALIFIED"),
                "CONVERTED", leadService.countLeadsByStatus("CONVERTED")
        ));
    }

    @GetMapping("/stats/hot-count")
    public ApiResponse<Long> countHotLeads() {
        return ApiResponse.success(leadService.countHotLeads());
    }
}
