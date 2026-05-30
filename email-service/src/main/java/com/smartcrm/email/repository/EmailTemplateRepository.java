package com.smartcrm.email.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartcrm.email.entity.EmailTemplate;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface EmailTemplateRepository extends BaseMapper<EmailTemplate> {
}