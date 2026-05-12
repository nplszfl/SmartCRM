package com.smartcrm.opportunity.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartcrm.opportunity.entity.Opportunity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OpportunityRepository extends BaseMapper<Opportunity> {
}
