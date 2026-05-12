package com.smartcrm.lead.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartcrm.lead.entity.Lead;
import org.apache.ibatis.annotations.Mapper;

/**
 * Lead repository using MyBatis Plus
 */
@Mapper
public interface LeadRepository extends BaseMapper<Lead> {
}
