package com.smartcrm.crm.service;

import com.smartcrm.crm.entity.Activity;
import com.smartcrm.crm.repository.ActivityRepository;
import com.smartcrm.common.exception.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Activity service implementation.
 */
@Slf4j
@Service
public class ActivityServiceImpl extends ServiceImpl<ActivityRepository, Activity> {

    public Activity createActivity(Activity activity) {
        log.info("Creating activity: {} for customer: {}", activity.getSubject(), activity.getCustomerId());
        this.save(activity);
        return activity;
    }

    public Activity updateActivity(Long id, Activity activity) {
        Activity existing = this.getById(id);
        if (existing == null) {
            throw new ResourceNotFoundException("Activity", id);
        }
        activity.setId(id);
        this.updateById(activity);
        return this.getById(id);
    }

    public Activity getActivityById(Long id) {
        return this.getById(id);
    }

    public List<Activity> getActivitiesByCustomerId(Long customerId) {
        return this.list(new LambdaQueryWrapper<Activity>()
                .eq(Activity::getCustomerId, customerId)
                .orderByDesc(Activity::getActivityDate));
    }

    public List<Activity> getActivitiesByContactId(Long contactId) {
        return this.list(new LambdaQueryWrapper<Activity>()
                .eq(Activity::getContactId, contactId)
                .orderByDesc(Activity::getActivityDate));
    }

    public List<Activity> getActivitiesByOwnerAndDateRange(Long ownerId, LocalDateTime start, LocalDateTime end) {
        return this.list(new LambdaQueryWrapper<Activity>()
                .eq(Activity::getOwnerId, ownerId)
                .between(Activity::getActivityDate, start, end)
                .orderByDesc(Activity::getActivityDate));
    }

    public void deleteActivity(Long id) {
        this.removeById(id);
    }

    public List<Activity> getRecentActivities(int limit) {
        return this.list(new LambdaQueryWrapper<Activity>()
                .orderByDesc(Activity::getActivityDate)
                .last("LIMIT " + limit));
    }
}