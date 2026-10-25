package com.fresh.interceptor;

import com.fresh.common.context.BaseContext;
import com.fresh.properties.JwtProperties;
import com.fresh.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 管理端 JWT 拦截器：校验 /admin/** 接口（登录除外）
 * token 载荷 key 为 empId
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenAdminInterceptor implements HandlerInterceptor {

    /** JWT 载荷中员工 id 的 key */
    public static final String CLAIM_EMP_ID = "empId";

    private final JwtProperties jwtProperties;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }
        String token = request.getHeader(jwtProperties.getAdminTokenName());
        try {
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            Claims claims = JwtUtil.parseJWT(jwtProperties.getAdminSecretKey(), token);
            Long empId = Long.valueOf(claims.get(CLAIM_EMP_ID).toString());
            BaseContext.setCurrentId(empId);
            return true;
        } catch (Exception e) {
            log.warn("管理端 token 校验失败: {}", e.getMessage());
            response.setStatus(401);
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        BaseContext.removeCurrentId();
    }
}
