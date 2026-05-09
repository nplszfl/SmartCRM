package com.smartcrm.crm.repository;

import com.smartcrm.crm.entity.Customer;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * Customer repository.
 */
@Mapper
public interface CustomerRepository extends BaseMapper<Customer> {

    List<Customer> findByOwnerId(@Param("ownerId") Long ownerId);

    List<Customer> findByCustomerType(@Param("customerType") String customerType);

    List<Customer> findByIndustry(@Param("industry") String industry);
}