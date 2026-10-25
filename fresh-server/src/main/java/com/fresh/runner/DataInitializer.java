package com.fresh.runner;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fresh.common.constant.RoleConstant;
import com.fresh.common.constant.StatusConstant;
import com.fresh.modules.admin.entity.Employee;
import com.fresh.modules.admin.mapper.EmployeeMapper;
import com.fresh.modules.user.entity.User;
import com.fresh.modules.user.mapper.UserMapper;
import com.fresh.utils.PasswordUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 数据初始化器：应用启动时检查并初始化基础账号
 * - employee 表：无 admin 则插入；存在但密码不是 123456 则重置
 * - user 表：无 user1（普通用户）/ leader1（团长）则插入，或修正密码为 123456
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    /** 初始默认密码 */
    private static final String DEFAULT_PASSWORD = "123456";

    private final EmployeeMapper employeeMapper;
    private final UserMapper userMapper;

    @Override
    public void run(String... args) {
        log.info("========== 开始检查初始化数据 ==========");
        initAdmin();
        initUser("user1", "普通用户", "13900000001", RoleConstant.USER);
        initUser("leader1", "团长一号", "13900000002", RoleConstant.LEADER);
        log.info("========== 初始化数据检查完成 ==========");
    }

    /**
     * 初始化管理员账号 admin
     */
    private void initAdmin() {
        Employee admin = employeeMapper.selectOne(
                new LambdaQueryWrapper<Employee>().eq(Employee::getUsername, "admin"));
        if (admin == null) {
            // 无 admin 账号则插入
            Employee employee = new Employee();
            employee.setUsername("admin");
            employee.setPassword(PasswordUtil.encode(DEFAULT_PASSWORD));
            employee.setName("管理员");
            employee.setPhone("13800000000");
            employee.setSex("1");
            employee.setStatus(StatusConstant.ENABLE);
            employee.setCreateTime(LocalDateTime.now());
            employee.setUpdateTime(LocalDateTime.now());
            employeeMapper.insert(employee);
            log.info("初始化管理员账号 admin 成功，默认密码 {}", DEFAULT_PASSWORD);
        } else if (!PasswordUtil.matches(DEFAULT_PASSWORD, admin.getPassword())) {
            // admin 存在但密码不是默认密码则重置
            admin.setPassword(PasswordUtil.encode(DEFAULT_PASSWORD));
            admin.setUpdateTime(LocalDateTime.now());
            employeeMapper.updateById(admin);
            log.info("管理员 admin 密码已重置为默认密码 {}", DEFAULT_PASSWORD);
        } else {
            log.info("管理员 admin 已存在，无需初始化");
        }
    }

    /**
     * 初始化 C 端演示账号（user1 普通用户 / leader1 团长）
     *
     * @param username 登录账号
     * @param name     昵称
     * @param phone    手机号
     * @param role     角色
     */
    private void initUser(String username, String name, String phone, Integer role) {
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        if (user == null) {
            User newUser = new User();
            newUser.setUsername(username);
            newUser.setPassword(PasswordUtil.encode(DEFAULT_PASSWORD));
            newUser.setName(name);
            newUser.setPhone(phone);
            newUser.setRole(role);
            newUser.setStatus(StatusConstant.ENABLE);
            newUser.setCreateTime(LocalDateTime.now());
            newUser.setUpdateTime(LocalDateTime.now());
            userMapper.insert(newUser);
            log.info("初始化用户 {} 成功，默认密码 {}", username, DEFAULT_PASSWORD);
        } else if (!PasswordUtil.matches(DEFAULT_PASSWORD, user.getPassword())) {
            user.setPassword(PasswordUtil.encode(DEFAULT_PASSWORD));
            user.setUpdateTime(LocalDateTime.now());
            userMapper.updateById(user);
            log.info("用户 {} 密码已重置为默认密码 {}", username, DEFAULT_PASSWORD);
        } else {
            log.info("用户 {} 已存在，无需初始化", username);
        }
    }
}
