package com.smartcrm.contract.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartcrm.contract.entity.Contract;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ContractRepository extends BaseMapper<Contract> {
}
