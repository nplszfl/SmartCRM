package com.smartcrm.crm.controller;

import com.smartcrm.crm.dto.CampaignRequest;
import com.smartcrm.crm.entity.Campaign;
import com.smartcrm.crm.service.CampaignService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * Campaign controller - manages marketing campaigns
 */
@RestController
@RequestMapping("/api/campaigns")
@RequiredArgsConstructor
public class CampaignController {

    private final CampaignService campaignService;

    @PostMapping
    public ResponseEntity<Campaign> createCampaign(@RequestBody CampaignRequest request) {
        return ResponseEntity.ok(campaignService.createCampaign(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Campaign> updateCampaign(@PathVariable Long id, @RequestBody CampaignRequest request) {
        return ResponseEntity.ok(campaignService.updateCampaign(id, request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Campaign> getCampaign(@PathVariable Long id) {
        return ResponseEntity.ok(campaignService.getCampaignById(id));
    }

    @GetMapping
    public ResponseEntity<List<Campaign>> getAllCampaigns() {
        return ResponseEntity.ok(campaignService.getAllCampaigns());
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Campaign>> getCampaignsByStatus(@PathVariable String status) {
        return ResponseEntity.ok(campaignService.getCampaignsByStatus(status));
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<Campaign>> getCampaignsByType(@PathVariable String type) {
        return ResponseEntity.ok(campaignService.getCampaignsByType(type));
    }

    @GetMapping("/owner/{ownerId}")
    public ResponseEntity<List<Campaign>> getCampaignsByOwner(@PathVariable Long ownerId) {
        return ResponseEntity.ok(campaignService.getCampaignsByOwner(ownerId));
    }

    @GetMapping("/active")
    public ResponseEntity<List<Campaign>> getActiveCampaigns() {
        return ResponseEntity.ok(campaignService.getActiveCampaigns());
    }

    @PostMapping("/{id}/activate")
    public ResponseEntity<Campaign> activateCampaign(@PathVariable Long id) {
        return ResponseEntity.ok(campaignService.activateCampaign(id));
    }

    @PostMapping("/{id}/pause")
    public ResponseEntity<Campaign> pauseCampaign(@PathVariable Long id) {
        return ResponseEntity.ok(campaignService.pauseCampaign(id));
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<Campaign> completeCampaign(@PathVariable Long id) {
        return ResponseEntity.ok(campaignService.completeCampaign(id));
    }

    @PutMapping("/{id}/metrics")
    public ResponseEntity<Campaign> updateMetrics(
            @PathVariable Long id,
            @RequestParam(required = false) Integer impressions,
            @RequestParam(required = false) Integer clicks,
            @RequestParam(required = false) Integer leadsGenerated,
            @RequestParam(required = false) Integer conversions,
            @RequestParam(required = false) BigDecimal revenue,
            @RequestParam(required = false) BigDecimal spent) {
        return ResponseEntity.ok(campaignService.updateMetrics(id, impressions, clicks, leadsGenerated, conversions, revenue, spent));
    }

    @PostMapping("/{id}/track-lead")
    public ResponseEntity<Campaign> trackLead(@PathVariable Long id) {
        return ResponseEntity.ok(campaignService.trackLead(id));
    }

    @PostMapping("/{id}/track-conversion")
    public ResponseEntity<Campaign> trackConversion(@PathVariable Long id) {
        return ResponseEntity.ok(campaignService.trackConversion(id));
    }

    @GetMapping("/{id}/roi")
    public ResponseEntity<BigDecimal> getCampaignRoi(@PathVariable Long id) {
        return ResponseEntity.ok(campaignService.calculateCampaignRoi(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCampaign(@PathVariable Long id) {
        campaignService.deleteCampaign(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/stats/total-budget")
    public ResponseEntity<BigDecimal> getTotalBudget() {
        return ResponseEntity.ok(campaignService.getTotalBudget());
    }

    @GetMapping("/stats/total-spent")
    public ResponseEntity<BigDecimal> getTotalSpent() {
        return ResponseEntity.ok(campaignService.getTotalSpent());
    }

    @GetMapping("/stats/total-revenue")
    public ResponseEntity<BigDecimal> getTotalRevenue() {
        return ResponseEntity.ok(campaignService.getTotalRevenue());
    }
}
