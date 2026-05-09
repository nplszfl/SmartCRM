package com.smartcrm.crm.repository;

import com.smartcrm.crm.entity.Activity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Activity repository.
 */
@Mapper
public interface ActivityRepository extends BaseMapper<Activity> {

    List<Activity> findByCustomerId(@Param("customerId") Long customerId);

    List<Activity> findByContactId(@Param("contactId") Long contactId);

    List<Activity> findByOwnerIdAndDateRange(
            @Param("ownerId") Long ownerId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    List<Activity> findByActivityType(@Param("activityType") String activityType);
}