package com.smartcrm.quote.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartcrm.quote.entity.QuoteLineItem;
import org.apache.ibatis.annotations.Mapper;

/**
 * Quote line item repository - MyBatis Plus mapper for QuoteLineItem entity
 */
@Mapper
public interface QuoteLineItemRepository extends BaseMapper<QuoteLineItem> {
}