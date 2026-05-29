package com.smartcrm.followup.service;

import com.smartcrm.followup.dto.BestFollowupTimeResponse;
import com.smartcrm.followup.dto.FollowupTaskRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AiFollowupServiceTest {

    private AiFollowupService aiFollowupService;

    @BeforeEach
    void setUp() {
        aiFollowupService = new AiFollowupService();
    }

    @Test
    void testSuggestTasks_returnsValidTasks() {
        // When
        List<FollowupTaskRequest> results = aiFollowupService.suggestTasks(1L, "Test Customer");

        // Then
        assertNotNull(results);
        assertFalse(results.isEmpty());
        assertTrue(results.size() >= 2);
        
        // Verify task structure
        FollowupTaskRequest firstTask = results.get(0);
        assertNotNull(firstTask.getTaskType());
        assertNotNull(firstTask.getTitle());
        assertNotNull(firstTask.getDueAt());
    }

    @Test
    void testSuggestBestFollowupTime_returnsValidResponse() {
        // When
        BestFollowupTimeResponse result = aiFollowupService.suggestBestFollowupTime(1L, "Test Customer");

        // Then
        assertNotNull(result);
        assertNotNull(result.getSuggestedTime());
        assertNotNull(result.getDayOfWeek());
        assertNotNull(result.getTimeSlot());
        assertNotNull(result.getReason());
        assertTrue(result.getConfidence() > 0);
    }

    @Test
    void testSuggestAction_forCallTask() {
        // When
        String result = aiFollowupService.suggestAction("CALL", "Test Customer");

        // Then
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertTrue(result.contains("短信") || result.contains("确认"));
    }

    @Test
    void testSuggestAction_forEmailTask() {
        // When
        String result = aiFollowupService.suggestAction("EMAIL", "Test Customer");

        // Then
        assertNotNull(result);
        assertTrue(result.contains("邮件") || result.contains("Email") || result.contains("主题"));
    }

    @Test
    void testSuggestAction_forMeetingTask() {
        // When
        String result = aiFollowupService.suggestAction("MEETING", "Test Customer");

        // Then
        assertNotNull(result);
        assertTrue(result.contains("会议") || result.contains("日历"));
    }
}