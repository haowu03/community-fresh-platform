package com.fresh.utils;

import cn.hutool.crypto.digest.BCrypt;

/**
 * 密码工具类：基于 BCrypt 的加密与校验
 */
public class PasswordUtil {

    /** 加密明文密码 */
    public static String encode(String rawPassword) {
        return BCrypt.hashpw(rawPassword, BCrypt.gensalt());
    }

    /** 校验明文与密文是否匹配 */
    public static boolean matches(String rawPassword, String encodedPassword) {
        return BCrypt.checkpw(rawPassword, encodedPassword);
    }
}
