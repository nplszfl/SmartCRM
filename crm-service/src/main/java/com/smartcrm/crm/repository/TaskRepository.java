package com.smartcrm.crm.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartcrm.crm.entity.Task;
import org.apache.ibatis.annotations.Mapper;

/**
 * Task repository.
 */
@Mapper
public interface TaskRepository extends BaseMapper<Task> {
}
