package com.smartcrm.followup.controller;

import com.smartcrm.common.dto.ApiResponse;
import com.smartcrm.followup.dto.BestFollowupTimeResponse;
import com.smartcrm.followup.dto.FollowupTaskRequest;
import com.smartcrm.followup.dto.FollowupTaskResponse;
import com.smartcrm.followup.service.AiFollowupService;
import com.smartcrm.followup.service.SmartFollowupService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Smart follow-up REST controller
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/followup")
public class SmartFollowupController {

    private final SmartFollowupService smartFollowupService;
    private final AiFollowupService aiFollowupService;

    @PostMapping("/tasks")
    public ApiResponse<FollowupTaskResponse> createTask(@RequestBody FollowupTaskRequest request) {
        FollowupTaskResponse response = smartFollowupService.createFollowupTask(request);
        return ApiResponse.success(response);
    }

    @PostMapping("/tasks/auto-generate")
    public ApiResponse<List<FollowupTaskResponse>> autoGenerateTasks(
            @RequestParam Long customerId,
            @RequestParam String customerName) {
        List<FollowupTaskResponse> tasks = smartFollowupService.autoGenerateTasks(customerId, customerName);
        return ApiResponse.success(tasks);
    }

    @GetMapping("/tasks/overdue")
    public ApiResponse<List<FollowupTaskResponse>> getOverdueTasks() {
        return ApiResponse.success(smartFollowupService.getOverdueTasks());
    }

    @GetMapping("/tasks/pending/{customerId}")
    public ApiResponse<List<FollowupTaskResponse>> getPendingTasks(@PathVariable Long customerId) {
        return ApiResponse.success(smartFollowupService.getPendingTasks(customerId));
    }

    @PostMapping("/tasks/{taskId}/complete")
    public ApiResponse<FollowupTaskResponse> completeTask(
            @PathVariable Long taskId,
            @RequestBody Map<String, String> body) {
        String note = body.getOrDefault("note", "");
        return ApiResponse.success(smartFollowupService.completeTask(taskId, note));
    }

    @GetMapping("/best-time")
    public ApiResponse<BestFollowupTimeResponse> getBestFollowupTime(
            @RequestParam Long customerId,
            @RequestParam String customerName) {
        return ApiResponse.success(aiFollowupService.suggestBestFollowupTime(customerId, customerName));
    }

    @GetMapping("/suggest-action")
    public ApiResponse<Map<String, String>> suggestAction(
            @RequestParam String taskType,
            @RequestParam String customerName) {
        String action = aiFollowupService.suggestAction(taskType, customerName);
        return ApiResponse.success(Map.of("suggestion", action));
    }
}