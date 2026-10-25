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
 * 用户端 JWT 拦截器：校验 /user/** 接口（登录注册等白名单除外）
 * token 载荷 key 为 userId
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenUserInterceptor implements HandlerInterceptor {

    /** JWT 载荷中用户 id 的 key */
    public static final String CLAIM_USER_ID = "userId";

    private final JwtProperties jwtProperties;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }
        String token = request.getHeader(jwtProperties.getUserTokenName());
        try {
            // 兼容 Bearer 前缀
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            Claims claims = JwtUtil.parseJWT(jwtProperties.getUserSecretKey(), token);
            Long userId = Long.valueOf(claims.get(CLAIM_USER_ID).toString());
            BaseContext.setCurrentId(userId);
            return true;
        } catch (Exception e) {
            log.warn("用户端 token 校验失败: {}", e.getMessage());
            response.setStatus(401);
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        BaseContext.removeCurrentId();
    }
}
