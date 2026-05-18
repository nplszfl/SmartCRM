package com.smartcrm.crm.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartcrm.crm.entity.Campaign;
import org.apache.ibatis.annotations.Mapper;

/**
 * Campaign repository.
 */
@Mapper
public interface CampaignRepository extends BaseMapper<Campaign> {
}
