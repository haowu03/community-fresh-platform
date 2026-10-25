package com.fresh.modules.admin.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fresh.common.constant.StatusConstant;
import com.fresh.common.context.BaseContext;
import com.fresh.common.exception.BusinessException;
import com.fresh.common.exception.LoginFailedException;
import com.fresh.common.result.PageResult;
import com.fresh.interceptor.JwtTokenAdminInterceptor;
import com.fresh.modules.admin.dto.EmployeeDTO;
import com.fresh.modules.admin.dto.EmployeeLoginDTO;
import com.fresh.modules.admin.dto.EmployeePageQueryDTO;
import com.fresh.modules.admin.dto.PasswordEditDTO;
import com.fresh.modules.admin.entity.Employee;
import com.fresh.modules.admin.mapper.EmployeeMapper;
import com.fresh.modules.admin.service.EmployeeService;
import com.fresh.modules.admin.vo.EmployeeLoginVO;
import com.fresh.properties.JwtProperties;
import com.fresh.utils.JwtUtil;
import com.fresh.utils.PasswordUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 员工 Service 实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl extends ServiceImpl<EmployeeMapper, Employee> implements EmployeeService {

    /** 新员工默认密码 */
    private static final String DEFAULT_PASSWORD = "123456";

    private final JwtProperties jwtProperties;

    /**
     * 员工登录：校验用户名、密码、状态，签发管理端 JWT
     */
    @Override
    public EmployeeLoginVO login(EmployeeLoginDTO loginDTO) {
        String username = loginDTO.getUsername();
        String password = loginDTO.getPassword();

        // 1. 根据用户名查询员工
        Employee employee = getOne(new LambdaQueryWrapper<Employee>().eq(Employee::getUsername, username));
        if (employee == null) {
            throw new LoginFailedException("账号不存在");
        }

        // 2. 校验密码（BCrypt）
        if (!PasswordUtil.matches(password, employee.getPassword())) {
            throw new LoginFailedException("密码错误");
        }

        // 3. 校验账号状态
        if (!StatusConstant.ENABLE.equals(employee.getStatus())) {
            throw new LoginFailedException("账号已被禁用");
        }

        // 4. 签发管理端 JWT，载荷中存放 empId
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtTokenAdminInterceptor.CLAIM_EMP_ID, employee.getId());
        String token = JwtUtil.createJWT(jwtProperties.getAdminSecretKey(), jwtProperties.getAdminTtl(), claims);

        return EmployeeLoginVO.builder()
                .id(employee.getId())
                .name(employee.getName())
                .token(token)
                .build();
    }

    /**
     * 员工分页查询：按姓名模糊匹配
     */
    @Override
    public PageResult pageQuery(EmployeePageQueryDTO pageQueryDTO) {
        Page<Employee> page = new Page<>(pageQueryDTO.getPage(), pageQueryDTO.getPageSize());
        LambdaQueryWrapper<Employee> wrapper = new LambdaQueryWrapper<Employee>()
                .like(StrUtil.isNotBlank(pageQueryDTO.getName()), Employee::getName, pageQueryDTO.getName())
                .orderByDesc(Employee::getCreateTime);
        Page<Employee> result = page(page, wrapper);
        // 密码脱敏，不下发到前端
        result.getRecords().forEach(e -> e.setPassword(null));
        return new PageResult(result.getTotal(), result.getRecords());
    }

    /**
     * 新增员工：校验用户名唯一，默认密码 123456 加密存储
     */
    @Override
    public void saveEmployee(EmployeeDTO employeeDTO) {
        // 校验用户名是否重复
        long count = count(new LambdaQueryWrapper<Employee>().eq(Employee::getUsername, employeeDTO.getUsername()));
        if (count > 0) {
            throw new BusinessException("用户名已存在");
        }

        Employee employee = new Employee();
        BeanUtils.copyProperties(employeeDTO, employee);
        // 默认密码 123456，BCrypt 加密存储
        employee.setPassword(PasswordUtil.encode(DEFAULT_PASSWORD));
        employee.setStatus(StatusConstant.ENABLE);
        employee.setCreateTime(LocalDateTime.now());
        employee.setUpdateTime(LocalDateTime.now());
        employee.setCreateUser(BaseContext.getCurrentId());
        employee.setUpdateUser(BaseContext.getCurrentId());
        save(employee);
    }

    /**
     * 修改员工信息
     */
    @Override
    public void updateEmployee(EmployeeDTO employeeDTO) {
        Employee employee = getById(employeeDTO.getId());
        if (employee == null) {
            throw new BusinessException("员工不存在");
        }
        BeanUtils.copyProperties(employeeDTO, employee);
        // 密码不在本接口修改
        employee.setPassword(null);
        employee.setUpdateTime(LocalDateTime.now());
        employee.setUpdateUser(BaseContext.getCurrentId());
        updateById(employee);
    }

    /**
     * 启用/禁用员工账号
     */
    @Override
    public void startOrStop(Integer status, Long id) {
        Employee employee = new Employee();
        employee.setId(id);
        employee.setStatus(status);
        updateById(employee);
    }

    /**
     * 修改当前登录员工密码：先校验旧密码
     */
    @Override
    public void editPassword(PasswordEditDTO passwordEditDTO) {
        Long empId = BaseContext.getCurrentId();
        Employee employee = getById(empId);
        if (employee == null) {
            throw new BusinessException("员工不存在");
        }
        // 校验旧密码
        if (!PasswordUtil.matches(passwordEditDTO.getOldPassword(), employee.getPassword())) {
            throw new BusinessException("旧密码错误");
        }
        // 新密码加密存储
        employee.setPassword(PasswordUtil.encode(passwordEditDTO.getNewPassword()));
        employee.setUpdateTime(LocalDateTime.now());
        updateById(employee);
    }
}
