package com.fresh.modules.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fresh.common.result.PageResult;
import com.fresh.modules.admin.dto.EmployeeDTO;
import com.fresh.modules.admin.dto.EmployeeLoginDTO;
import com.fresh.modules.admin.dto.EmployeePageQueryDTO;
import com.fresh.modules.admin.dto.PasswordEditDTO;
import com.fresh.modules.admin.entity.Employee;
import com.fresh.modules.admin.vo.EmployeeLoginVO;

/**
 * 员工 Service
 */
public interface EmployeeService extends IService<Employee> {

    /**
     * 员工登录，签发管理端 JWT
     *
     * @param loginDTO 登录参数
     * @return 登录结果（含 token）
     */
    EmployeeLoginVO login(EmployeeLoginDTO loginDTO);

    /**
     * 员工分页查询
     *
     * @param pageQueryDTO 分页参数
     * @return 分页结果
     */
    PageResult pageQuery(EmployeePageQueryDTO pageQueryDTO);

    /**
     * 新增员工（默认密码 123456）
     *
     * @param employeeDTO 员工信息
     */
    void saveEmployee(EmployeeDTO employeeDTO);

    /**
     * 修改员工信息
     *
     * @param employeeDTO 员工信息
     */
    void updateEmployee(EmployeeDTO employeeDTO);

    /**
     * 启用/禁用员工账号
     *
     * @param status 状态 1启用 0禁用
     * @param id     员工id
     */
    void startOrStop(Integer status, Long id);

    /**
     * 修改当前登录员工密码
     *
     * @param passwordEditDTO 旧密码/新密码
     */
    void editPassword(PasswordEditDTO passwordEditDTO);
}
