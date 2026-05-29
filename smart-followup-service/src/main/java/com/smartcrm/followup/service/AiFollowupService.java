package com.smartcrm.followup.service;

import com.smartcrm.followup.dto.BestFollowupTimeResponse;
import com.smartcrm.followup.dto.FollowupTaskRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * AI-powered follow-up analysis service (mock implementation for demo)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiFollowupService {

    /**
     * Suggest follow-up tasks for a customer
     */
    public List<FollowupTaskRequest> suggestTasks(Long customerId, String customerName) {
        log.info("Suggesting follow-up tasks for customer: {}", customerId);

        List<FollowupTaskRequest> tasks = new ArrayList<>();

        // Generate context-aware follow-up tasks
        tasks.add(createTask(customerId, customerName, "CALL", "HIGH",
                "客户需求跟进电话",
                "主动联系客户，了解项目进展和近期需求",
                LocalDateTime.now().plusDays(1)));

        tasks.add(createTask(customerId, customerName, "EMAIL", "MEDIUM",
                "发送项目更新邮件",
                "整理项目最新进展，发送邮件供客户确认",
                LocalDateTime.now().plusDays(2)));

        tasks.add(createTask(customerId, customerName, "MEETING", "HIGH",
                "预约在线会议",
                "邀请客户参加线上会议，讨论下一步合作计划",
                LocalDateTime.now().plusDays(3)));

        // Add additional tasks based on customer engagement
        tasks.add(createTask(customerId, customerName, "DEMO", "MEDIUM",
                "产品功能演示",
                "针对客户关注的功能进行专项演示",
                LocalDateTime.now().plusDays(5)));

        return tasks;
    }

    /**
     * Suggest best follow-up time
     */
    public BestFollowupTimeResponse suggestBestFollowupTime(Long customerId, String customerName) {
        log.info("Suggesting best follow-up time for customer: {}", customerId);

        // Calculate optimal time based on business rules
        LocalDateTime suggestedTime = calculateOptimalTime();

        return BestFollowupTimeResponse.builder()
                .customerId(customerId)
                .customerName(customerName)
                .suggestedTime(suggestedTime)
                .dayOfWeek(suggestedTime.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.CHINESE))
                .timeSlot(getTimeSlot(suggestedTime.getHour()))
                .reason("根据销售最佳实践，周二至周四上午10-11点、下午3-4点是联系客户的最佳时间，决策者通常在这些时段更available")
                .confidence(0.75)
                .alternativeTimes(buildAlternativeTimes())
                .build();
    }

    /**
     * Suggest action for task type
     */
    public String suggestAction(String taskType, String customerName) {
        return switch (taskType) {
            case "CALL" -> "建议在通话前5分钟发送短信确认，提醒客户即将通话讨论重要事项";
            case "EMAIL" -> "邮件主题要简洁明确，正文控制在200字以内，突出客户价值";
            case "MEETING" -> "提前15分钟进入会议室，准备好议程和讨论材料，发送日历邀请";
            case "DEMO" -> "演示前做好测试准备，根据客户行业定制演示内容有的放矢";
            case "PROPOSAL" -> "方案要针对客户痛点定制，突出ROI和实施时间线，准备FAQ应对质疑";
            default -> "按照标准流程执行，保持专业态度";
        };
    }

    private FollowupTaskRequest createTask(Long customerId, String customerName,
            String taskType, String priority, String title, String description, LocalDateTime dueAt) {
        FollowupTaskRequest request = new FollowupTaskRequest();
        request.setCustomerId(customerId);
        request.setCustomerName(customerName);
        request.setTaskType(taskType);
        request.setPriority(priority);
        request.setTitle(title);
        request.setDescription(description);
        request.setDueAt(dueAt);
        return request;
    }

    private LocalDateTime calculateOptimalTime() {
        LocalDateTime now = LocalDateTime.now();
        int dayOfWeek = now.getDayOfWeek().getValue();

        // Find next Tuesday-Thursday
        LocalDateTime candidate = now.plusDays(1);
        while (candidate.getDayOfWeek() == DayOfWeek.SATURDAY ||
               candidate.getDayOfWeek() == DayOfWeek.SUNDAY ||
               candidate.getDayOfWeek() == DayOfWeek.FRIDAY) {
            candidate = candidate.plusDays(1);
        }

        // Set to 10:00 AM or 3:00 PM
        int targetHour = Math.random() > 0.5 ? 10 : 15;
        return candidate.withHour(targetHour).withMinute(0).withSecond(0);
    }

    private String getTimeSlot(int hour) {
        if (hour >= 9 && hour < 12) return "上午";
        if (hour >= 12 && hour < 14) return "中午";
        if (hour >= 14 && hour < 18) return "下午";
        if (hour >= 18 && hour < 21) return "晚上";
        return "其他时段";
    }

    private String buildAlternativeTimes() {
        return """
            [
                {"dayOfWeek": "周三", "time": "下午3点", "reason": "决策者通常在周三下午时间较充裕"},
                {"dayOfWeek": "周四", "time": "上午10点", "reason": "周初忙碌过后，周四效率最高"},
                {"dayOfWeek": "周五", "time": "上午11点", "reason": "周末前可确认事项，适合快速决策"}
            ]
            """;
    }
}