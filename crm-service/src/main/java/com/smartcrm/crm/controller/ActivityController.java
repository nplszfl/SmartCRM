package com.smartcrm.crm.controller;

import com.smartcrm.common.dto.ApiResponse;
import com.smartcrm.crm.entity.Activity;
import com.smartcrm.crm.service.ActivityServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Activity REST controller.
 */
@Slf4j
@RestController
@RequestMapping("/activities")
public class ActivityController {

    private final ActivityServiceImpl activityService;

    public ActivityController(ActivityServiceImpl activityService) {
        this.activityService = activityService;
    }

    @PostMapping
    public ApiResponse<Activity> createActivity(@RequestBody Activity activity) {
        log.info("REST request to create activity: {}", activity.getSubject());
        Activity created = activityService.createActivity(activity);
        return ApiResponse.success(created);
    }

    @PutMapping("/{id}")
    public ApiResponse<Activity> updateActivity(@PathVariable Long id, @RequestBody Activity activity) {
        log.info("REST request to update activity: {}", id);
        Activity updated = activityService.updateActivity(id, activity);
        return ApiResponse.success(updated);
    }

    @GetMapping("/{id}")
    public ApiResponse<Activity> getActivity(@PathVariable Long id) {
        log.info("REST request to get activity: {}", id);
        Activity activity = activityService.getActivityById(id);
        return ApiResponse.success(activity);
    }

    @GetMapping("/customer/{customerId}")
    public ApiResponse<List<Activity>> getActivitiesByCustomer(@PathVariable Long customerId) {
        log.info("REST request to get activities for customer: {}", customerId);
        List<Activity> activities = activityService.getActivitiesByCustomerId(customerId);
        return ApiResponse.success(activities);
    }

    @GetMapping("/contact/{contactId}")
    public ApiResponse<List<Activity>> getActivitiesByContact(@PathVariable Long contactId) {
        log.info("REST request to get activities for contact: {}", contactId);
        List<Activity> activities = activityService.getActivitiesByContactId(contactId);
        return ApiResponse.success(activities);
    }

    @GetMapping("/owner/{ownerId}")
    public ApiResponse<List<Activity>> getActivitiesByOwnerAndDateRange(
            @PathVariable Long ownerId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        log.info("REST request to get activities for owner: {} from {} to {}", ownerId, start, end);
        List<Activity> activities = activityService.getActivitiesByOwnerAndDateRange(ownerId, start, end);
        return ApiResponse.success(activities);
    }

    @GetMapping("/recent")
    public ApiResponse<List<Activity>> getRecentActivities(@RequestParam(defaultValue = "20") int limit) {
        log.info("REST request to get recent activities, limit: {}", limit);
        List<Activity> activities = activityService.getRecentActivities(limit);
        return ApiResponse.success(activities);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteActivity(@PathVariable Long id) {
        log.info("REST request to delete activity: {}", id);
        activityService.deleteActivity(id);
        return ApiResponse.success("Activity deleted successfully", null);
    }
}