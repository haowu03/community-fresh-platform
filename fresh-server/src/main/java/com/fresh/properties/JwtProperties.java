package com.fresh.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * JWT 配置属性（用户端 / 管理端双体系）
 */
@Component
@ConfigurationProperties(prefix = "fresh.jwt")
@Data
public class JwtProperties {

    /** 管理端加密密钥 */
    private String adminSecretKey;
    /** 管理端 token 有效期（毫秒） */
    private long adminTtl;
    /** 管理端 token 请求头名称 */
    private String adminTokenName;

    /** 用户端加密密钥 */
    private String userSecretKey;
    /** 用户端 token 有效期（毫秒） */
    private long userTtl;
    /** 用户端 token 请求头名称 */
    private String userTokenName;
}
