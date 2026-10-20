package com.fresh.common.context;

/**
 * 基于 ThreadLocal 的登录用户上下文
 * 拦截器解析 JWT 后写入，业务层通过 BaseContext.getCurrentId() 获取
 */
public class BaseContext {

    private static final ThreadLocal<Long> THREAD_LOCAL = new ThreadLocal<>();

    public static void setCurrentId(Long id) {
        THREAD_LOCAL.set(id);
    }

    public static Long getCurrentId() {
        return THREAD_LOCAL.get();
    }

    public static void removeCurrentId() {
        THREAD_LOCAL.remove();
    }
}
