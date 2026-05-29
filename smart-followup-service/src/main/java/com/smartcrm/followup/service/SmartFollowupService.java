package com.smartcrm.followup.service;

import com.smartcrm.followup.dto.BestFollowupTimeResponse;
import com.smartcrm.followup.dto.FollowupTaskRequest;
import com.smartcrm.followup.dto.FollowupTaskResponse;
import com.smartcrm.followup.entity.FollowupTask;
import com.smartcrm.followup.mapper.FollowupTaskMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Smart follow-up service with AI-powered task generation and timing
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SmartFollowupService {

    private final FollowupTaskMapper followupTaskMapper;
    private final AiFollowupService aiFollowupService;

    /**
     * Auto-generate follow-up tasks for customer
     */
    public List<FollowupTaskResponse> autoGenerateTasks(Long customerId, String customerName) {
        log.info("Auto-generating follow-up tasks for customer: {}", customerId);

        // Use AI to determine follow-up tasks needed
        List<FollowupTaskRequest> taskRequests = aiFollowupService.suggestTasks(customerId, customerName);

        return taskRequests.stream()
                .map(this::createTask)
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Create a single follow-up task
     */
    public FollowupTaskResponse createFollowupTask(FollowupTaskRequest request) {
        FollowupTask task = new FollowupTask();
        task.setCustomerId(request.getCustomerId());
        task.setCustomerName(request.getCustomerName());
        task.setOpportunityId(request.getOpportunityId());
        task.setTaskType(request.getTaskType());
        task.setPriority(request.getPriority() != null ? request.getPriority() : "MEDIUM");
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setDueAt(request.getDueAt() != null ? request.getDueAt() : LocalDateTime.now().plusDays(1));
        task.setStatus("PENDING");

        // AI recommended time
        BestFollowupTimeResponse bestTime = aiFollowupService.suggestBestFollowupTime(
                request.getCustomerId(), request.getCustomerName());
        if (bestTime != null && bestTime.getSuggestedTime() != null) {
            task.setAiRecommendedTime(bestTime.getSuggestedTime());
            task.setAiRecommendationReason(bestTime.getReason());
        }

        followupTaskMapper.insert(task);
        return mapToResponse(task);
    }

    /**
     * Get overdue tasks
     */
    public List<FollowupTaskResponse> getOverdueTasks() {
        LocalDateTime now = LocalDateTime.now();
        List<FollowupTask> overdueTasks = followupTaskMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<FollowupTask>()
                        .eq(FollowupTask::getStatus, "PENDING")
                        .lt(FollowupTask::getDueAt, now)
        );

        return overdueTasks.stream()
                .map(this::enrichWithOverdueInfo)
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get pending tasks for customer
     */
    public List<FollowupTaskResponse> getPendingTasks(Long customerId) {
        List<FollowupTask> tasks = followupTaskMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<FollowupTask>()
                        .eq(FollowupTask::getCustomerId, customerId)
                        .eq(FollowupTask::getStatus, "PENDING")
                        .orderByAsc(FollowupTask::getDueAt)
        );

        return tasks.stream()
                .map(this::enrichWithOverdueInfo)
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Complete a follow-up task
     */
    public FollowupTaskResponse completeTask(Long taskId, String note) {
        FollowupTask task = followupTaskMapper.selectById(taskId);
        if (task != null) {
            task.setStatus("COMPLETED");
            task.setCompletedAt(LocalDateTime.now());
            task.setCompletedNote(note);
            followupTaskMapper.updateById(task);
        }
        return mapToResponse(task);
    }

    private FollowupTask createTask(FollowupTaskRequest request) {
        FollowupTask task = new FollowupTask();
        task.setCustomerId(request.getCustomerId());
        task.setCustomerName(request.getCustomerName());
        task.setOpportunityId(request.getOpportunityId());
        task.setTaskType(request.getTaskType());
        task.setPriority(request.getPriority() != null ? request.getPriority() : "MEDIUM");
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setDueAt(request.getDueAt() != null ? request.getDueAt() : LocalDateTime.now().plusDays(1));
        task.setStatus("PENDING");
        task.setSuggestedAction(aiFollowupService.suggestAction(request.getTaskType(), request.getCustomerName()));

        followupTaskMapper.insert(task);
        return task;
    }

    private FollowupTask enrichWithOverdueInfo(FollowupTask task) {
        if ("PENDING".equals(task.getStatus()) && task.getDueAt().isBefore(LocalDateTime.now())) {
            long hours = java.time.Duration.between(task.getDueAt(), LocalDateTime.now()).toHours();
            task.setOverdueHours((int) hours);
        }
        return task;
    }

    private FollowupTaskResponse mapToResponse(FollowupTask task) {
        if (task == null) return null;

        FollowupTaskResponse.FollowupTaskResponseBuilder builder = FollowupTaskResponse.builder()
                .id(task.getId())
                .customerId(task.getCustomerId())
                .customerName(task.getCustomerName())
                .opportunityId(task.getOpportunityId())
                .taskType(task.getTaskType())
                .priority(task.getPriority())
                .status(task.getStatus())
                .title(task.getTitle())
                .description(task.getDescription())
                .suggestedAction(task.getSuggestedAction())
                .dueAt(task.getDueAt())
                .completedAt(task.getCompletedAt())
                .aiRecommendedTime(task.getAiRecommendedTime())
                .aiRecommendationReason(task.getAiRecommendationReason());

        if (task.getOverdueHours() != null) {
            builder.overdueHours(task.getOverdueHours());
            builder.overdueWarning(task.getOverdueHours() > 48 ? "紧急跟进" : "需要跟进");
        }

        return builder.build();
    }
}