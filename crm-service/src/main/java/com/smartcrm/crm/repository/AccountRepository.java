package com.smartcrm.crm.repository;

import com.smartcrm.crm.entity.Account;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Optional;

/**
 * Account repository.
 */
@Mapper
public interface AccountRepository extends BaseMapper<Account> {

    Optional<Account> findByAccountNumber(@Param("accountNumber") String accountNumber);

    List<Account> findByCustomerId(@Param("customerId") Long customerId);

    List<Account> findByStatus(@Param("status") String status);
}