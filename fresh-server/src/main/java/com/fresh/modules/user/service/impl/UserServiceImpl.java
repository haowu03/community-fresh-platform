package com.fresh.modules.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fresh.common.constant.RoleConstant;
import com.fresh.common.constant.StatusConstant;
import com.fresh.common.context.BaseContext;
import com.fresh.common.exception.BusinessException;
import com.fresh.common.exception.LoginFailedException;
import com.fresh.interceptor.JwtTokenUserInterceptor;
import com.fresh.modules.user.dto.UserInfoDTO;
import com.fresh.modules.user.dto.UserLoginDTO;
import com.fresh.modules.user.dto.UserRegisterDTO;
import com.fresh.modules.user.entity.User;
import com.fresh.modules.user.mapper.UserMapper;
import com.fresh.modules.user.service.UserService;
import com.fresh.modules.user.vo.UserInfoVO;
import com.fresh.modules.user.vo.UserLoginVO;
import com.fresh.properties.JwtProperties;
import com.fresh.utils.JwtUtil;
import com.fresh.utils.PasswordUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 用户 Service 实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final JwtProperties jwtProperties;

    /**
     * 账号密码登录：校验用户名、密码、状态，签发用户端 JWT
     */
    @Override
    public UserLoginVO login(UserLoginDTO loginDTO) {
        String username = loginDTO.getUsername();
        String password = loginDTO.getPassword();

        // 1. 根据用户名查询用户
        User user = getOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        if (user == null) {
            throw new LoginFailedException("账号不存在");
        }

        // 2. 校验密码（BCrypt）
        if (!PasswordUtil.matches(password, user.getPassword())) {
            throw new LoginFailedException("密码错误");
        }

        // 3. 校验账号状态
        if (!StatusConstant.ENABLE.equals(user.getStatus())) {
            throw new LoginFailedException("账号已被禁用");
        }

        // 4. 签发用户端 JWT，载荷中存放 userId
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtTokenUserInterceptor.CLAIM_USER_ID, user.getId());
        String token = JwtUtil.createJWT(jwtProperties.getUserSecretKey(), jwtProperties.getUserTtl(), claims);

        return UserLoginVO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .name(user.getName())
                .role(user.getRole())
                .token(token)
                .build();
    }

    /**
     * 用户注册：校验用户名唯一，密码加密存储，默认普通用户角色
     */
    @Override
    public void register(UserRegisterDTO registerDTO) {
        // 校验用户名是否重复
        long count = count(new LambdaQueryWrapper<User>().eq(User::getUsername, registerDTO.getUsername()));
        if (count > 0) {
            throw new BusinessException("用户名已存在");
        }

        User user = new User();
        BeanUtils.copyProperties(registerDTO, user);
        // 密码 BCrypt 加密存储
        user.setPassword(PasswordUtil.encode(registerDTO.getPassword()));
        // 默认普通用户、状态正常；昵称默认为账户名
        user.setRole(RoleConstant.USER);
        user.setStatus(StatusConstant.ENABLE);
        if (!StringUtils.hasText(user.getName())) {
            user.setName(user.getUsername());
        }
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        save(user);
    }

    /**
     * 查询当前登录用户信息
     */
    @Override
    public UserInfoVO info() {
        Long userId = BaseContext.getCurrentId();
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return UserInfoVO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .name(user.getName())
                .phone(user.getPhone())
                .sex(user.getSex())
                .avatar(user.getAvatar())
                .role(user.getRole())
                .createTime(user.getCreateTime())
                .build();
    }

    /**
     * 修改当前登录用户个人信息（昵称/手机号/性别/头像）
     */
    @Override
    public void updateInfo(UserInfoDTO userInfoDTO) {
        Long userId = BaseContext.getCurrentId();
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        BeanUtils.copyProperties(userInfoDTO, user);
        user.setUpdateTime(LocalDateTime.now());
        updateById(user);
    }
}
