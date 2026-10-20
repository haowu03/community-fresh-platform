package com.fresh.common.exception;

/**
 * 登录失败异常：账号不存在、密码错误、账号被禁用
 */
public class LoginFailedException extends BaseException {

    public LoginFailedException(String msg) {
        super(msg);
    }
}
