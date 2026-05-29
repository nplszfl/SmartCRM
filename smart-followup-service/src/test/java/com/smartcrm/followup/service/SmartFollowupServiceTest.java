package com.smartcrm.followup.service;

import com.smartcrm.followup.dto.BestFollowupTimeResponse;
import com.smartcrm.followup.dto.FollowupTaskRequest;
import com.smartcrm.followup.dto.FollowupTaskResponse;
import com.smartcrm.followup.entity.FollowupTask;
import com.smartcrm.followup.mapper.FollowupTaskMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SmartFollowupServiceTest {

    @Mock
    private FollowupTaskMapper followupTaskMapper;

    @Mock
    private AiFollowupService aiFollowupService;

    @InjectMocks
    private SmartFollowupService smartFollowupService;

    private FollowupTaskRequest sampleRequest;

    @BeforeEach
    void setUp() {
        sampleRequest = new FollowupTaskRequest();
        sampleRequest.setCustomerId(1L);
        sampleRequest.setCustomerName("Test Customer");
        sampleRequest.setTaskType("CALL");
        sampleRequest.setPriority("HIGH");
        sampleRequest.setTitle("Follow-up Call");
        sampleRequest.setDescription("Schedule a call to discuss opportunity");
        sampleRequest.setDueAt(LocalDateTime.now().plusDays(1));
    }

    @Test
    void testCreateFollowupTask() {
        // Given
        when(followupTaskMapper.insert(any())).thenReturn(1);
        when(aiFollowupService.suggestBestFollowupTime(any(), any())).thenReturn(
                BestFollowupTimeResponse.builder()
                        .suggestedTime(LocalDateTime.now().plusDays(2))
                        .reason("Best time based on analysis")
                        .build()
        );

        // When
        FollowupTaskResponse result = smartFollowupService.createFollowupTask(sampleRequest);

        // Then
        assertNotNull(result);
        assertEquals("CALL", result.getTaskType());
        assertEquals("HIGH", result.getPriority());
        assertEquals("Follow-up Call", result.getTitle());
        assertEquals("PENDING", result.getStatus());
        verify(followupTaskMapper, times(1)).insert(any(FollowupTask.class));
    }

    @Test
    void testAutoGenerateTasks() {
        // Given
        when(aiFollowupService.suggestTasks(any(), any())).thenReturn(List.of(sampleRequest));
        when(followupTaskMapper.insert(any())).thenReturn(1);
        when(aiFollowupService.suggestAction(any(), any())).thenReturn("Call customer to discuss");

        // When
        List<FollowupTaskResponse> results = smartFollowupService.autoGenerateTasks(1L, "Test Customer");

        // Then
        assertNotNull(results);
        assertFalse(results.isEmpty());
    }

    @Test
    void testGetOverdueTasks() {
        // Given
        FollowupTask overdueTask = new FollowupTask();
        overdueTask.setId(1L);
        overdueTask.setCustomerId(1L);
        overdueTask.setCustomerName("Test Customer");
        overdueTask.setTaskType("CALL");
        overdueTask.setStatus("PENDING");
        overdueTask.setDueAt(LocalDateTime.now().minusHours(48));
        overdueTask.setTitle("Overdue Task");

        when(followupTaskMapper.selectList(any())).thenReturn(List.of(overdueTask));

        // When
        List<FollowupTaskResponse> results = smartFollowupService.getOverdueTasks();

        // Then
        assertNotNull(results);
        assertFalse(results.isEmpty());
        assertNotNull(results.get(0).getOverdueHours());
        assertTrue(results.get(0).getOverdueHours() > 0);
    }

    @Test
    void testCompleteTask() {
        // Given
        FollowupTask task = new FollowupTask();
        task.setId(1L);
        task.setCustomerId(1L);
        task.setCustomerName("Test Customer");
        task.setTaskType("CALL");
        task.setStatus("PENDING");
        task.setTitle("Task to Complete");

        when(followupTaskMapper.selectById(1L)).thenReturn(task);
        when(followupTaskMapper.updateById(any())).thenReturn(1);

        // When
        FollowupTaskResponse result = smartFollowupService.completeTask(1L, "Completed successfully");

        // Then
        assertNotNull(result);
        assertEquals("COMPLETED", result.getStatus());
    }
}