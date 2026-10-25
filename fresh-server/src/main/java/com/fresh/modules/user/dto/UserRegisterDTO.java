package com.fresh.modules.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 用户注册请求参数
 */
@Data
@Schema(description = "用户注册请求参数")
public class UserRegisterDTO implements Serializable {

    /** 登录账号 */
    @Schema(description = "登录账号", requiredMode = Schema.RequiredMode.REQUIRED)
    private String username;

    /** 密码 */
    @Schema(description = "密码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;

    /** 昵称 */
    @Schema(description = "昵称")
    private String name;

    /** 手机号 */
    @Schema(description = "手机号")
    private String phone;
}
