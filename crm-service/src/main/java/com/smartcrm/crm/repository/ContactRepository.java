package com.smartcrm.crm.repository;

import com.smartcrm.crm.entity.Contact;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * Contact repository.
 */
@Mapper
public interface ContactRepository extends BaseMapper<Contact> {

    List<Contact> findByCustomerId(@Param("customerId") Long customerId);

    List<Contact> findByEmail(@Param("email") String email);

    List<Contact> findPrimaryContacts(@Param("customerId") Long customerId);
}