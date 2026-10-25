package com.fresh.modules.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fresh.modules.admin.entity.Employee;
import org.apache.ibatis.annotations.Mapper;

/**
 * 员工 Mapper
 */
@Mapper
public interface EmployeeMapper extends BaseMapper<Employee> {
}
