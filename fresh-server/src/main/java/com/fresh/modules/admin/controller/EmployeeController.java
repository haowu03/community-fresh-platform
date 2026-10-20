package com.fresh.modules.admin.controller;

import com.fresh.common.result.PageResult;
import com.fresh.common.result.R;
import com.fresh.modules.admin.dto.EmployeeDTO;
import com.fresh.modules.admin.dto.EmployeeLoginDTO;
import com.fresh.modules.admin.dto.EmployeePageQueryDTO;
import com.fresh.modules.admin.dto.PasswordEditDTO;
import com.fresh.modules.admin.service.EmployeeService;
import com.fresh.modules.admin.vo.EmployeeLoginVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理端-员工接口：登录、员工管理、密码修改
 */
@Slf4j
@RestController
@RequestMapping("/admin/employee")
@RequiredArgsConstructor
@Tag(name = "员工模块", description = "管理端-员工认证与管理接口")
public class EmployeeController {

    private final EmployeeService employeeService;

    /**
     * 员工登录
     */
    @PostMapping("/login")
    @Operation(summary = "员工登录")
    public R<EmployeeLoginVO> login(@RequestBody EmployeeLoginDTO loginDTO) {
        log.info("员工登录: {}", loginDTO.getUsername());
        EmployeeLoginVO loginVO = employeeService.login(loginDTO);
        return R.success(loginVO);
    }

    /**
     * 退出登录（前端清除 token 即可，这里直接返回成功）
     */
    @PostMapping("/logout")
    @Operation(summary = "退出登录")
    public R<Void> logout() {
        return R.success();
    }

    /**
     * 员工分页查询
     */
    @GetMapping("/page")
    @Operation(summary = "员工分页查询")
    public R<PageResult> page(EmployeePageQueryDTO pageQueryDTO) {
        PageResult pageResult = employeeService.pageQuery(pageQueryDTO);
        return R.success(pageResult);
    }

    /**
     * 新增员工
     */
    @PostMapping
    @Operation(summary = "新增员工")
    public R<Void> save(@RequestBody EmployeeDTO employeeDTO) {
        employeeService.saveEmployee(employeeDTO);
        return R.success();
    }

    /**
     * 修改员工信息
     */
    @PutMapping
    @Operation(summary = "修改员工")
    public R<Void> update(@RequestBody EmployeeDTO employeeDTO) {
        employeeService.updateEmployee(employeeDTO);
        return R.success();
    }

    /**
     * 启用/禁用员工账号
     */
    @PutMapping("/status/{status}")
    @Operation(summary = "启用禁用员工账号")
    public R<Void> startOrStop(@PathVariable Integer status, Long id) {
        employeeService.startOrStop(status, id);
        return R.success();
    }

    /**
     * 修改当前登录员工密码
     */
    @PutMapping("/editPassword")
    @Operation(summary = "修改密码")
    public R<Void> editPassword(@RequestBody PasswordEditDTO passwordEditDTO) {
        employeeService.editPassword(passwordEditDTO);
        return R.success();
    }
}
