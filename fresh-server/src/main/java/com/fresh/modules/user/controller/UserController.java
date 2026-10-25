package com.fresh.modules.user.controller;

import com.fresh.common.result.R;
import com.fresh.modules.user.dto.UserInfoDTO;
import com.fresh.modules.user.dto.UserLoginDTO;
import com.fresh.modules.user.dto.UserRegisterDTO;
import com.fresh.modules.user.service.UserService;
import com.fresh.modules.user.vo.UserInfoVO;
import com.fresh.modules.user.vo.UserLoginVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户端-用户接口：登录、注册、个人信息
 */
@Slf4j
@RestController
@RequestMapping("/user/user")
@RequiredArgsConstructor
@Tag(name = "用户模块", description = "用户端-登录/注册/个人信息接口")
public class UserController {

    private final UserService userService;

    /**
     * 账号密码登录
     */
    @PostMapping("/login")
    @Operation(summary = "账号密码登录")
    public R<UserLoginVO> login(@RequestBody UserLoginDTO loginDTO) {
        log.info("用户登录: {}", loginDTO.getUsername());
        UserLoginVO loginVO = userService.login(loginDTO);
        return R.success(loginVO);
    }

    /**
     * 用户注册
     */
    @PostMapping("/register")
    @Operation(summary = "用户注册")
    public R<Void> register(@RequestBody UserRegisterDTO registerDTO) {
        log.info("用户注册: {}", registerDTO.getUsername());
        userService.register(registerDTO);
        return R.success();
    }

    /**
     * 查询当前登录用户信息
     */
    @GetMapping("/info")
    @Operation(summary = "当前用户信息")
    public R<UserInfoVO> info() {
        UserInfoVO infoVO = userService.info();
        return R.success(infoVO);
    }

    /**
     * 修改当前登录用户个人信息
     */
    @PutMapping
    @Operation(summary = "修改个人信息")
    public R<Void> updateInfo(@RequestBody UserInfoDTO userInfoDTO) {
        userService.updateInfo(userInfoDTO);
        return R.success();
    }
}
