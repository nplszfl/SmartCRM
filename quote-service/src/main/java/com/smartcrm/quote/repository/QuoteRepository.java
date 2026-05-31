package com.smartcrm.quote.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartcrm.quote.entity.Quote;
import org.apache.ibatis.annotations.Mapper;

/**
 * Quote repository - MyBatis Plus mapper for Quote entity
 */
@Mapper
public interface QuoteRepository extends BaseMapper<Quote> {
}