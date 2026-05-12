package com.smartcrm.opportunity.controller;

import com.smartcrm.common.dto.ApiResponse;
import com.smartcrm.opportunity.dto.OpportunityRequest;
import com.smartcrm.opportunity.entity.Opportunity;
import com.smartcrm.opportunity.service.OpportunityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Opportunity REST controller
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/opportunities")
public class OpportunityController {

    private final OpportunityService opportunityService;

    @PostMapping
    public ApiResponse<Opportunity> createOpportunity(@Valid @RequestBody OpportunityRequest request) {
        Opportunity opp = opportunityService.createOpportunity(request);
        return ApiResponse.success(opp);
    }

    @PutMapping("/{id}")
    public ApiResponse<Opportunity> updateOpportunity(@PathVariable Long id, @Valid @RequestBody OpportunityRequest request) {
        Opportunity opp = opportunityService.updateOpportunity(id, request);
        return ApiResponse.success(opp);
    }

    @GetMapping("/{id}")
    public ApiResponse<Opportunity> getOpportunity(@PathVariable Long id) {
        Opportunity opp = opportunityService.getOpportunityById(id);
        return ApiResponse.success(opp);
    }

    @GetMapping
    public ApiResponse<List<Opportunity>> getAllOpportunities() {
        List<Opportunity> opps = opportunityService.getAllOpportunities();
        return ApiResponse.success(opps);
    }

    @GetMapping("/stage/{stage}")
    public ApiResponse<List<Opportunity>> getByStage(@PathVariable String stage) {
        List<Opportunity> opps = opportunityService.getOpportunitiesByStage(stage);
        return ApiResponse.success(opps);
    }

    @GetMapping("/owner/{ownerId}")
    public ApiResponse<List<Opportunity>> getByOwner(@PathVariable Long ownerId) {
        List<Opportunity> opps = opportunityService.getOpportunitiesByOwner(ownerId);
        return ApiResponse.success(opps);
    }

    @GetMapping("/account/{accountId}")
    public ApiResponse<List<Opportunity>> getByAccount(@PathVariable Long accountId) {
        List<Opportunity> opps = opportunityService.getOpportunitiesByAccount(accountId);
        return ApiResponse.success(opps);
    }

    @PatchMapping("/{id}/stage")
    public ApiResponse<Opportunity> moveToStage(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String stage = body.get("stage");
        Opportunity opp = opportunityService.moveToStage(id, stage);
        return ApiResponse.success(opp);
    }

    @PostMapping("/{id}/close-won")
    public ApiResponse<Opportunity> closeAsWon(@PathVariable Long id) {
        Opportunity opp = opportunityService.closeAsWon(id);
        return ApiResponse.success(opp);
    }

    @PostMapping("/{id}/close-lost")
    public ApiResponse<Opportunity> closeAsLost(@PathVariable Long id) {
        Opportunity opp = opportunityService.closeAsLost(id);
        return ApiResponse.success(opp);
    }

    @PatchMapping("/{id}/ai-prediction")
    public ApiResponse<Opportunity> updateAiPrediction(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        String prediction = (String) body.get("prediction");
        Double confidence = ((Number) body.get("confidence")).doubleValue();
        Opportunity opp = opportunityService.updateAiPrediction(id, prediction, confidence);
        return ApiResponse.success(opp);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteOpportunity(@PathVariable Long id) {
        opportunityService.deleteOpportunity(id);
        return ApiResponse.success();
    }

    @GetMapping("/stats/pipeline-value")
    public ApiResponse<BigDecimal> getTotalPipelineValue() {
        return ApiResponse.success(opportunityService.getTotalPipelineValue());
    }

    @GetMapping("/stats/weighted-pipeline-value")
    public ApiResponse<BigDecimal> getWeightedPipelineValue() {
        return ApiResponse.success(opportunityService.getWeightedPipelineValue());
    }

    @GetMapping("/stats/count-by-stage")
    public ApiResponse<Map<String, Long>> countByStage() {
        return ApiResponse.success(Map.of(
                "PROSPECTING", opportunityService.countByStage("PROSPECTING"),
                "QUALIFICATION", opportunityService.countByStage("QUALIFICATION"),
                "PROPOSAL", opportunityService.countByStage("PROPOSAL"),
                "NEGOTIATION", opportunityService.countByStage("NEGOTIATION"),
                "CLOSED_WON", opportunityService.countByStage("CLOSED_WON"),
                "CLOSED_LOST", opportunityService.countByStage("CLOSED_LOST")
        ));
    }
}
