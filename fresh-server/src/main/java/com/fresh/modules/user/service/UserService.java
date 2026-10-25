package com.fresh.modules.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fresh.modules.user.dto.UserInfoDTO;
import com.fresh.modules.user.dto.UserLoginDTO;
import com.fresh.modules.user.dto.UserRegisterDTO;
import com.fresh.modules.user.entity.User;
import com.fresh.modules.user.vo.UserInfoVO;
import com.fresh.modules.user.vo.UserLoginVO;

/**
 * 用户 Service
 */
public interface UserService extends IService<User> {

    /**
     * 账号密码登录，签发用户端 JWT
     *
     * @param loginDTO 登录参数
     * @return 登录结果（含 token）
     */
    UserLoginVO login(UserLoginDTO loginDTO);

    /**
     * 用户注册
     *
     * @param registerDTO 注册参数
     */
    void register(UserRegisterDTO registerDTO);

    /**
     * 查询当前登录用户信息
     *
     * @return 用户信息
     */
    UserInfoVO info();

    /**
     * 修改当前登录用户个人信息
     *
     * @param userInfoDTO 个人信息
     */
    void updateInfo(UserInfoDTO userInfoDTO);
}
